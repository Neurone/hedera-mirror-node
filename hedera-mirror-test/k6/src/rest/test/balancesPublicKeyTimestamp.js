/*
 * Copyright (C) 2024 Hedera Hashgraph, LLC
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

import http from 'k6/http';

import {isValidListResponse, RestTestScenarioBuilder} from '../libex/common.js';
import {balanceListName} from '../libex/constants.js';

const urlTag = '/balances?account.publickey={accountId}&timestamp={timestamp}';

const getUrl = (testParameters) =>
  `/balances?account.publickey=${testParameters['DEFAULT_PUBLIC_KEY']}&timestamp=${testParameters['DEFAULT_BALANCE_TIMESTAMP']}`;

const {options, run, setup} = new RestTestScenarioBuilder()
  .name('balancesPublicKeyTimestamp') // use unique scenario name among all tests
  .tags({url: urlTag})
  .request((testParameters) => {
    const url = `${testParameters['BASE_URL_PREFIX']}${getUrl(testParameters)}`;
    return http.get(url);
  })
  .requiredParameters('DEFAULT_PUBLIC_KEY', 'DEFAULT_BALANCE_TIMESTAMP')
  .check('Balances OK', (r) => isValidListResponse(r, balanceListName))
  .build();

export {getUrl, options, run, setup};