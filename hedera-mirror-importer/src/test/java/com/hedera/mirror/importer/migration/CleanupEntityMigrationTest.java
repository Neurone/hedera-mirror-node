/*
 * Copyright (C) 2021-2025 Hedera Hashgraph, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hedera.mirror.importer.migration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.hedera.mirror.common.domain.entity.Entity;
import com.hedera.mirror.common.domain.entity.EntityId;
import com.hedera.mirror.common.domain.entity.EntityType;
import com.hedera.mirror.common.domain.transaction.Transaction;
import com.hedera.mirror.common.domain.transaction.TransactionType;
import com.hedera.mirror.importer.DisableRepeatableSqlMigration;
import com.hedera.mirror.importer.EnabledIfV1;
import com.hedera.mirror.importer.ImporterIntegrationTest;
import com.hedera.mirror.importer.ImporterProperties;
import com.hedera.mirror.importer.config.Owner;
import com.hedera.mirror.importer.repository.EntityRepository;
import com.hedera.mirror.importer.repository.TransactionRepository;
import com.hederahashgraph.api.proto.java.ResponseCodeEnum;
import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.test.context.TestPropertySource;

@DisablePartitionMaintenance
@DisableRepeatableSqlMigration
@EnabledIfV1
@RequiredArgsConstructor
@Tag("migration")
@TestPropertySource(properties = "spring.flyway.target=1.35.5")
class CleanupEntityMigrationTest extends ImporterIntegrationTest {

    private final @Owner JdbcOperations jdbcOperations;

    @Value("classpath:db/migration/v1/V1.36.2__entities_update.sql")
    private final File migrationSql;

    private final EntityRepository entityRepository;
    private final ImporterProperties importerProperties;
    private final TransactionRepository transactionRepository;

    @BeforeEach
    void before() {
        importerProperties.setStartDate(Instant.EPOCH);
        importerProperties.setEndDate(Instant.EPOCH.plusSeconds(1));
        setEntityTablesPreV_1_36();
    }

    @AfterEach
    void after() {
        cleanup();
    }

    @Test
    void verifyEntityTypeMigrationEmpty() throws Exception {
        // migration
        migrate();

        assertEquals(0, entityRepository.count());
        assertEquals(0, transactionRepository.count());
    }

    @Test
    void verifyEntityMigrationCreationTransactions() throws Exception {
        long[] ids = new long[] {1, 2, 3, 4, 5, 6};
        insertEntity(entity(ids[0], EntityType.ACCOUNT));
        insertEntity(entity(ids[1], EntityType.CONTRACT));
        insertEntity(entity(ids[2], EntityType.FILE));
        insertEntity(entity(ids[3], EntityType.TOPIC));
        insertEntity(entity(ids[4], EntityType.TOKEN));
        insertEntity(entity(ids[5], EntityType.SCHEDULE));

        long[] createTimestamps = new long[] {10, 20, 30, 40, 50, 60};
        insertTransaction(createTimestamps[0], 1, ResponseCodeEnum.SUCCESS, TransactionType.CRYPTOCREATEACCOUNT);
        insertTransaction(createTimestamps[1], 2, ResponseCodeEnum.SUCCESS, TransactionType.CONTRACTCREATEINSTANCE);
        insertTransaction(createTimestamps[2], 3, ResponseCodeEnum.SUCCESS, TransactionType.FILECREATE);
        insertTransaction(createTimestamps[3], 4, ResponseCodeEnum.SUCCESS, TransactionType.CONSENSUSCREATETOPIC);
        insertTransaction(createTimestamps[4], 5, ResponseCodeEnum.SUCCESS, TransactionType.TOKENCREATION);
        insertTransaction(createTimestamps[5], 6, ResponseCodeEnum.SUCCESS, TransactionType.SCHEDULECREATE);

        assertEquals(ids.length, getTEntitiesCount());

        // migration
        migrate();

        assertEquals(createTimestamps.length, entityRepository.count());
        for (int i = 0; i < ids.length; i++) {
            Optional<Entity> entity = retrieveEntity(ids[i]);
            long consensusTimestamp = createTimestamps[i];
            assertAll(() -> assertThat(entity)
                    .isPresent()
                    .get()
                    .returns(consensusTimestamp, Entity::getCreatedTimestamp)
                    .returns(consensusTimestamp, Entity::getTimestampLower)
                    .returns("", Entity::getMemo));
        }
    }

    private Optional<Entity> retrieveEntity(Long id) {
        return Optional.of(jdbcOperations.queryForObject(
                "select * from entity where id = ?",
                (rs, rowNum) -> {
                    Entity entity = new Entity();
                    entity.setAutoRenewAccountId(rs.getLong("auto_renew_account_id"));
                    entity.setAutoRenewPeriod(rs.getLong("auto_renew_period"));
                    entity.setCreatedTimestamp(rs.getLong("created_timestamp"));
                    entity.setDeleted(rs.getBoolean("deleted"));
                    entity.setExpirationTimestamp(rs.getLong("expiration_timestamp"));
                    entity.setId(rs.getLong("id"));
                    entity.setKey(rs.getBytes("key"));
                    entity.setMemo(rs.getString("memo"));
                    entity.setTimestampLower(rs.getLong("modified_timestamp"));
                    entity.setNum(rs.getLong("num"));
                    entity.setPublicKey(rs.getString("public_key"));
                    entity.setRealm(rs.getLong("realm"));
                    entity.setShard(rs.getLong("shard"));
                    entity.setSubmitKey(rs.getBytes("submit_key"));
                    entity.setType(EntityType.fromId(rs.getInt("type")));
                    return entity;
                },
                id));
    }

    @Test
    void verifyEntityMigrationCreationTransactionsWithFailures() throws Exception {
        long[] ids = new long[] {1, 2, 3, 4, 5, 6};
        insertEntity(entity(ids[0], EntityType.ACCOUNT));
        insertEntity(entity(ids[1], EntityType.CONTRACT));
        insertEntity(entity(ids[2], EntityType.FILE));
        insertEntity(entity(ids[3], EntityType.TOPIC));
        insertEntity(entity(ids[4], EntityType.TOKEN));
        insertEntity(entity(ids[5], EntityType.SCHEDULE));

        long[] createTimestamps = new long[] {10, 20, 30, 40, 50, 60};

        // failed create transactions
        insertTransaction(
                createTimestamps[0] - 1,
                1,
                ResponseCodeEnum.INSUFFICIENT_ACCOUNT_BALANCE,
                TransactionType.CRYPTOCREATEACCOUNT);
        insertTransaction(
                createTimestamps[1] - 1,
                2,
                ResponseCodeEnum.INVALID_TRANSACTION,
                TransactionType.CONTRACTCREATEINSTANCE);
        insertTransaction(
                createTimestamps[2] - 1, 3, ResponseCodeEnum.PAYER_ACCOUNT_NOT_FOUND, TransactionType.FILECREATE);
        insertTransaction(
                createTimestamps[3] - 1,
                4,
                ResponseCodeEnum.INVALID_NODE_ACCOUNT,
                TransactionType.CONSENSUSCREATETOPIC);
        insertTransaction(
                createTimestamps[4] - 1, 5, ResponseCodeEnum.INVALID_SIGNATURE, TransactionType.TOKENCREATION);
        insertTransaction(createTimestamps[5] - 1, 6, ResponseCodeEnum.MEMO_TOO_LONG, TransactionType.SCHEDULECREATE);

        // successful create transactions
        insertTransaction(createTimestamps[0], 1, ResponseCodeEnum.SUCCESS, TransactionType.CRYPTOCREATEACCOUNT);
        insertTransaction(createTimestamps[1], 2, ResponseCodeEnum.SUCCESS, TransactionType.CONTRACTCREATEINSTANCE);
        insertTransaction(createTimestamps[2], 3, ResponseCodeEnum.SUCCESS, TransactionType.FILECREATE);
        insertTransaction(createTimestamps[3], 4, ResponseCodeEnum.SUCCESS, TransactionType.CONSENSUSCREATETOPIC);
        insertTransaction(createTimestamps[4], 5, ResponseCodeEnum.SUCCESS, TransactionType.TOKENCREATION);
        insertTransaction(createTimestamps[5], 6, ResponseCodeEnum.SUCCESS, TransactionType.SCHEDULECREATE);

        // migration
        migrate();

        assertEquals(createTimestamps.length, entityRepository.count());
        for (int i = 0; i < ids.length; i++) {
            Optional<Entity> entity = retrieveEntity(ids[i]);
            long consensusTimestamp = createTimestamps[i];
            assertAll(() -> assertThat(entity)
                    .isPresent()
                    .get()
                    .returns(consensusTimestamp, Entity::getCreatedTimestamp)
                    .returns(consensusTimestamp, Entity::getTimestampLower)
                    .returns("", Entity::getMemo));
        }
    }

    @Test
    void verifyEntityMigrationWithSingleUpdate() throws Exception {
        // excludes schedules as they can't yet be updated
        long[] ids = new long[] {1, 2, 3, 4, 5};
        insertEntity(entity(ids[0], EntityType.ACCOUNT));
        insertEntity(entity(ids[1], EntityType.CONTRACT));
        insertEntity(entity(ids[2], EntityType.FILE));
        insertEntity(entity(ids[3], EntityType.TOPIC));
        insertEntity(entity(ids[4], EntityType.TOKEN));

        long[] createTimestamps = new long[] {10, 20, 30, 40, 50};

        // successful create transactions
        insertTransaction(createTimestamps[0], 1, ResponseCodeEnum.SUCCESS, TransactionType.CRYPTOCREATEACCOUNT);
        insertTransaction(createTimestamps[1], 2, ResponseCodeEnum.SUCCESS, TransactionType.CONTRACTCREATEINSTANCE);
        insertTransaction(createTimestamps[2], 3, ResponseCodeEnum.SUCCESS, TransactionType.FILECREATE);
        insertTransaction(createTimestamps[3], 4, ResponseCodeEnum.SUCCESS, TransactionType.CONSENSUSCREATETOPIC);
        insertTransaction(createTimestamps[4], 5, ResponseCodeEnum.SUCCESS, TransactionType.TOKENCREATION);

        // successful update transactions
        long[] modifiedTimestamps = new long[] {110, 120, 130, 140, 150};
        insertTransaction(modifiedTimestamps[0], 1, ResponseCodeEnum.SUCCESS, TransactionType.CRYPTOUPDATEACCOUNT);
        insertTransaction(modifiedTimestamps[1], 2, ResponseCodeEnum.SUCCESS, TransactionType.CONTRACTUPDATEINSTANCE);
        insertTransaction(modifiedTimestamps[2], 3, ResponseCodeEnum.SUCCESS, TransactionType.FILEUPDATE);
        insertTransaction(modifiedTimestamps[3], 4, ResponseCodeEnum.SUCCESS, TransactionType.CONSENSUSUPDATETOPIC);
        insertTransaction(modifiedTimestamps[4], 5, ResponseCodeEnum.SUCCESS, TransactionType.TOKENUPDATE);

        // migration
        migrate();

        assertEquals(createTimestamps.length, entityRepository.count());
        for (int i = 0; i < ids.length; i++) {
            Optional<Entity> entity = retrieveEntity(ids[i]);
            long createdTimestamp = createTimestamps[i];
            long modifiedTimestamp = modifiedTimestamps[i];
            assertAll(() -> assertThat(entity)
                    .isPresent()
                    .get()
                    .returns(createdTimestamp, Entity::getCreatedTimestamp)
                    .returns(modifiedTimestamp, Entity::getTimestampLower));
        }
    }

    @Test
    void verifyEntityMigrationWithMultipleUpdates() throws Exception {
        // excludes schedules as they can't yet be updated
        long[] ids = new long[] {1, 2, 3, 4, 5};
        insertEntity(entity(ids[0], EntityType.ACCOUNT));
        insertEntity(entity(ids[1], EntityType.CONTRACT));
        insertEntity(entity(ids[2], EntityType.FILE));
        insertEntity(entity(ids[3], EntityType.TOPIC));
        insertEntity(entity(ids[4], EntityType.TOKEN));

        long[] createTimestamps = new long[] {10, 20, 30, 40, 50};

        // successful create transactions
        insertTransaction(createTimestamps[0], 1, ResponseCodeEnum.SUCCESS, TransactionType.CRYPTOCREATEACCOUNT);
        insertTransaction(createTimestamps[1], 2, ResponseCodeEnum.SUCCESS, TransactionType.CONTRACTCREATEINSTANCE);
        insertTransaction(createTimestamps[2], 3, ResponseCodeEnum.SUCCESS, TransactionType.FILECREATE);
        insertTransaction(createTimestamps[3], 4, ResponseCodeEnum.SUCCESS, TransactionType.CONSENSUSCREATETOPIC);
        insertTransaction(createTimestamps[4], 5, ResponseCodeEnum.SUCCESS, TransactionType.TOKENCREATION);

        // successful update transactions
        long[] modifiedTimestamps = new long[] {110, 120, 130, 140, 150};
        insertTransaction(modifiedTimestamps[0], 1, ResponseCodeEnum.SUCCESS, TransactionType.CRYPTOUPDATEACCOUNT);
        insertTransaction(modifiedTimestamps[1], 2, ResponseCodeEnum.SUCCESS, TransactionType.CONTRACTUPDATEINSTANCE);
        insertTransaction(modifiedTimestamps[2], 3, ResponseCodeEnum.SUCCESS, TransactionType.FILEUPDATE);
        insertTransaction(modifiedTimestamps[3], 4, ResponseCodeEnum.SUCCESS, TransactionType.CONSENSUSUPDATETOPIC);
        insertTransaction(modifiedTimestamps[4], 5, ResponseCodeEnum.SUCCESS, TransactionType.TOKENUPDATE);

        long[] deletedTimestamps = new long[] {210, 220, 230, 240, 250};
        insertTransaction(deletedTimestamps[0], 1, ResponseCodeEnum.SUCCESS, TransactionType.CRYPTODELETE);
        insertTransaction(deletedTimestamps[1], 2, ResponseCodeEnum.SUCCESS, TransactionType.CONTRACTDELETEINSTANCE);
        insertTransaction(deletedTimestamps[2], 3, ResponseCodeEnum.SUCCESS, TransactionType.FILEDELETE);
        insertTransaction(deletedTimestamps[3], 4, ResponseCodeEnum.SUCCESS, TransactionType.CONSENSUSDELETETOPIC);
        insertTransaction(deletedTimestamps[4], 5, ResponseCodeEnum.SUCCESS, TransactionType.TOKENDELETION);

        // migration
        migrate();

        assertEquals(createTimestamps.length, entityRepository.count());
        for (int i = 0; i < ids.length; i++) {
            Optional<Entity> entity = retrieveEntity(ids[i]);
            long createdTimestamp = createTimestamps[i];
            long modifiedTimestamp = deletedTimestamps[i];
            assertAll(() -> assertThat(entity)
                    .isPresent()
                    .get()
                    .returns(createdTimestamp, Entity::getCreatedTimestamp)
                    .returns(modifiedTimestamp, Entity::getTimestampLower));
        }
    }

    private Transaction transaction(
            long consensusNs, long entityNum, ResponseCodeEnum result, TransactionType transactionType) {
        Transaction transaction = new Transaction();
        transaction.setChargedTxFee(100L);
        transaction.setConsensusTimestamp(consensusNs);
        transaction.setEntityId(EntityId.of(0, 0, entityNum));
        transaction.setInitialBalance(1000L);
        transaction.setMemo("transaction memo".getBytes());
        transaction.setNodeAccountId(EntityId.of(0, 1, 3));
        transaction.setPayerAccountId(EntityId.of(0, 1, 98));
        transaction.setResult(result.getNumber());
        transaction.setType(transactionType.getProtoId());
        transaction.setValidStartNs(20L);
        transaction.setValidDurationSeconds(11L);
        transaction.setMaxFee(33L);
        return transaction;
    }

    private void insertTransaction(
            long consensusTimestamp, long entityNum, ResponseCodeEnum result, TransactionType transactionType) {
        Transaction transaction = transaction(consensusTimestamp, entityNum, result, transactionType);
        jdbcOperations.update(
                "insert into transaction (charged_tx_fee, consensus_ns, entity_id, initial_balance, max_fee, "
                        + "memo, "
                        + "node_account_id, payer_account_id, result, scheduled, transaction_bytes, "
                        + "transaction_hash, type, valid_duration_seconds, valid_start_ns)"
                        + " values"
                        + " (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                transaction.getChargedTxFee(),
                transaction.getConsensusTimestamp(),
                transaction.getEntityId().getId(),
                transaction.getInitialBalance(),
                transaction.getMaxFee(),
                transaction.getMemo(),
                transaction.getNodeAccountId().getId(),
                transaction.getPayerAccountId().getId(),
                transaction.getResult(),
                transaction.isScheduled(),
                transaction.getTransactionBytes(),
                transaction.getTransactionHash(),
                transaction.getType(),
                transaction.getValidDurationSeconds(),
                transaction.getValidStartNs());
    }

    private Entity entity(long id, EntityType entityType) {
        Entity entity = new Entity();
        entity.setId(id);
        entity.setNum(id);
        entity.setRealm(0L);
        entity.setShard(0L);
        entity.setType(entityType);
        entity.setAutoRenewAccountId(EntityId.of(1L, 2L, 3L).getId());
        entity.setProxyAccountId(EntityId.of("4.5.6"));
        return entity;
    }

    private void migrate() throws IOException {
        jdbcOperations.update(FileUtils.readFileToString(migrationSql, "UTF-8"));
    }

    /**
     * Insert entity object using only columns supported before V_1_36.2
     *
     * @param entity entity domain
     */
    private void insertEntity(Entity entity) {
        jdbcOperations.update(
                "insert into t_entities (auto_renew_account_id, auto_renew_period, deleted, entity_num, "
                        + "entity_realm, entity_shard, ed25519_public_key_hex, exp_time_ns, fk_entity_type_id, "
                        + "id, key, memo, proxy_account_id, submit_key) values"
                        + " (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                entity.getAutoRenewAccountId(),
                entity.getAutoRenewPeriod(),
                entity.getDeleted() != null && entity.getDeleted(),
                entity.getNum(),
                entity.getRealm(),
                entity.getShard(),
                entity.getPublicKey(),
                entity.getExpirationTimestamp(),
                entity.getType().getId(),
                entity.getId(),
                entity.getKey(),
                entity.getMemo(),
                entity.getProxyAccountId().getId(),
                entity.getSubmitKey());
    }

    /**
     * Ensure entity tables match expected state before V_1_36.2
     */
    private void setEntityTablesPreV_1_36() {
        // remove entity table if present
        jdbcOperations.execute("drop table if exists entity cascade;");

        // add t_entities if not present
        jdbcOperations.execute(
                """
    create table if not exists t_entities (
        entity_num             bigint  not null,
        entity_realm           bigint  not null,
        entity_shard           bigint  not null,
        fk_entity_type_id      integer not null,
        auto_renew_period      bigint,
        key                    bytea,
        deleted                boolean default false,
        exp_time_ns            bigint,
        ed25519_public_key_hex character varying,
        submit_key             bytea,
        memo                   text,
        auto_renew_account_id  bigint,
        id                     bigint  not null,
        proxy_account_id       bigint
    );
""");
    }

    private int getTEntitiesCount() {
        return jdbcOperations.queryForObject("select count(*) from t_entities", Integer.class);
    }

    private void cleanup() {
        jdbcOperations.execute("truncate table entity restart identity cascade;");

        jdbcOperations.execute("drop table if exists t_entities cascade;");
    }
}
