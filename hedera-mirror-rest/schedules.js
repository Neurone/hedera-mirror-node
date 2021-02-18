/*
 *
 * Hedera Mirror Node
 *  ​
 * Copyright (C) 2019 - 2021 Hedera Hashgraph, LLC
 *  ​
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
 *
 */

'use strict';

const constants = require('./constants');
const EntityId = require('./entityId');
const utils = require('./utils');
const {InvalidArgumentError} = require('./errors/invalidArgumentError');
const {NotFoundError} = require('./errors/notFoundError');

const scheduleSelectFields = [
  'e.key',
  's.consensus_timestamp',
  'creator_account_id',
  'executed_timestamp',
  'e.memo',
  'payer_account_id',
  's.schedule_id',
  'transaction_body',
  `json_agg(
    json_build_object(
      'consensus_timestamp', ss.consensus_timestamp::text,
      'public_key_prefix', encode(ss.public_key_prefix, 'base64'),
      'signature', encode(ss.signature, 'base64')
    ) order by ss.consensus_timestamp
  ) as signatures`,
];

const entityIdJoinQuery = 'join t_entities e on e.id = s.schedule_id';
const groupByQuery = 'group by e.key, e.memo, s.consensus_timestamp, s.schedule_id';
const scheduleIdMatchQuery = 'where s.schedule_id = $1';
const scheduleSelectQuery = ['select', scheduleSelectFields.join(',\n'), 'from schedule s'].join('\n');
const signatureJoinQuery = 'left join schedule_signature ss on ss.schedule_id = s.schedule_id';

const getScheduleByIdQuery = [
  scheduleSelectQuery,
  entityIdJoinQuery,
  signatureJoinQuery,
  scheduleIdMatchQuery,
  groupByQuery,
].join('\n');

const formatScheduleRow = (row) => {
  const signatures = row.signatures
    .filter((signature) => signature.consensus_timestamp !== null)
    .map((signature) => {
      return {
        consensus_timestamp: utils.nsToSecNs(signature.consensus_timestamp),
        public_key_prefix: signature.public_key_prefix,
        signature: signature.signature,
      };
    });

  return {
    admin_key: utils.encodeKey(row.key),
    consensus_timestamp: utils.nsToSecNs(row.consensus_timestamp),
    creator_account_id: EntityId.fromString(row.creator_account_id).toString(),
    executed_timestamp: row.executed_timestamp === null ? null : utils.nsToSecNs(row.executed_timestamp),
    memo: row.memo,
    payer_account_id: EntityId.fromString(row.payer_account_id).toString(),
    schedule_id: EntityId.fromString(row.schedule_id).toString(),
    signatures,
    transaction_body: utils.encodeBase64(row.transaction_body),
  };
};

/**
 * Handler function for /schedules/:id API
 * @param {Request} req HTTP request object
 * @param {Response} res HTTP response object
 * @returns {Promise<void>}
 */
const getScheduleById = async (req, res) => {
  const scheduleId = req.params.id;
  if (!utils.isValidEntityNum(scheduleId)) {
    throw InvalidArgumentError.forParams(constants.filterKeys.SCHEDULEID);
  }

  const encodedScheduleId = EntityId.fromString(scheduleId).getEncodedId();
  if (logger.isTraceEnabled()) {
    logger.trace(`getScheduleById query: ${getScheduleByIdQuery}, params: ${encodedScheduleId}`);
  }
  const {rows} = await utils.queryQuietly(getScheduleByIdQuery, encodedScheduleId);
  if (rows.length !== 1) {
    throw new NotFoundError();
  }

  res.locals[constants.responseDataLabel] = formatScheduleRow(rows[0]);
};

module.exports = {
  getScheduleById,
};

if (utils.isTestEnv()) {
  Object.assign(module.exports, {
    formatScheduleRow,
  });
}
