// SPDX-License-Identifier: Apache-2.0

import http from 'k6/http';

import {isValidListResponse, RestTestScenarioBuilder} from '../libex/common.js';
import {messageListName} from '../libex/constants.js';

const urlTag = '/topics/{id}/messages?sequencenumber={sequenceNumber}';

const getUrl = (testParameters) =>
  `/topics/${testParameters['DEFAULT_TOPIC_ID']}/messages?sequencenumber=${testParameters['DEFAULT_TOPIC_SEQUENCE']}`;

const {options, run, setup} = new RestTestScenarioBuilder()
  .name('topicsIdMessagesSequenceQueryParam') // use unique scenario name among all tests
  .tags({url: urlTag})
  .request((testParameters) => {
    const url = `${testParameters['BASE_URL_PREFIX']}${getUrl(testParameters)}`;
    return http.get(url);
  })
  .requiredParameters('DEFAULT_TOPIC_ID', 'DEFAULT_TOPIC_SEQUENCE')
  .check('Topics id messages sequenceNumber query param OK', (r) => isValidListResponse(r, messageListName))
  .build();

export {getUrl, options, run, setup};
