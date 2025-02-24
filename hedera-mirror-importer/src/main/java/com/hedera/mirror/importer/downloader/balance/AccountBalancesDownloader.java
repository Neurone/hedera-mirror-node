// SPDX-License-Identifier: Apache-2.0

package com.hedera.mirror.importer.downloader.balance;

import com.hedera.mirror.common.domain.balance.AccountBalance;
import com.hedera.mirror.common.domain.balance.AccountBalanceFile;
import com.hedera.mirror.importer.ImporterProperties;
import com.hedera.mirror.importer.addressbook.ConsensusNode;
import com.hedera.mirror.importer.addressbook.ConsensusNodeService;
import com.hedera.mirror.importer.config.DateRangeCalculator;
import com.hedera.mirror.importer.domain.StreamFileData;
import com.hedera.mirror.importer.downloader.Downloader;
import com.hedera.mirror.importer.downloader.NodeSignatureVerifier;
import com.hedera.mirror.importer.downloader.StreamFileNotifier;
import com.hedera.mirror.importer.downloader.provider.StreamFileProvider;
import com.hedera.mirror.importer.leader.Leader;
import com.hedera.mirror.importer.reader.balance.BalanceFileReader;
import com.hedera.mirror.importer.reader.signature.SignatureFileReader;
import com.hedera.mirror.importer.repository.AccountBalanceFileRepository;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.inject.Named;
import java.util.concurrent.atomic.AtomicBoolean;
import org.springframework.scheduling.annotation.Scheduled;

@Named
public class AccountBalancesDownloader extends Downloader<AccountBalanceFile, AccountBalance> {

    private final AccountBalanceFileRepository accountBalanceFileRepository;
    private final AtomicBoolean accountBalanceFileExists = new AtomicBoolean(false);

    @SuppressWarnings("java:S107")
    public AccountBalancesDownloader(
            AccountBalanceFileRepository accountBalanceFileRepository,
            ConsensusNodeService consensusNodeService,
            BalanceDownloaderProperties downloaderProperties,
            ImporterProperties importerProperties,
            MeterRegistry meterRegistry,
            DateRangeCalculator dateRangeCalculator,
            NodeSignatureVerifier nodeSignatureVerifier,
            SignatureFileReader signatureFileReader,
            StreamFileNotifier streamFileNotifier,
            StreamFileProvider streamFileProvider,
            BalanceFileReader streamFileReader) {
        super(
                consensusNodeService,
                downloaderProperties,
                importerProperties,
                meterRegistry,
                dateRangeCalculator,
                nodeSignatureVerifier,
                signatureFileReader,
                streamFileNotifier,
                streamFileProvider,
                streamFileReader);
        this.accountBalanceFileRepository = accountBalanceFileRepository;
    }

    @Override
    @Leader
    @Scheduled(fixedDelayString = "#{@balanceDownloaderProperties.getFrequency().toMillis()}")
    public void download() {
        downloadNextBatch();
    }

    @Override
    protected void onVerified(StreamFileData streamFileData, AccountBalanceFile streamFile, ConsensusNode node) {
        super.onVerified(streamFileData, streamFile, node);
        accountBalanceFileExists.set(true);
    }

    @Override
    protected boolean shouldDownload() {
        if (downloaderProperties.isEnabled()) {
            return true;
        }

        if (accountBalanceFileExists.get()) {
            return false;
        }

        if (accountBalanceFileRepository.findLatest().isPresent()) {
            accountBalanceFileExists.set(true);
            return false;
        }

        return true;
    }
}
