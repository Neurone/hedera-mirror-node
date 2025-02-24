// SPDX-License-Identifier: Apache-2.0

const setupTestParameters = (requiredParameters) => {
  const testParameters = {
    BASE_URL_PREFIX: __ENV.BASE_URL_PREFIX,
    DEFAULT_ACCOUNT_ID_NFTS_ALLOWANCE_OWNER: __ENV['DEFAULT_ACCOUNT_ID_NFTS_ALLOWANCE_OWNER'],
    DEFAULT_ACCOUNT_ID_NFTS_ALLOWANCE_SPENDER: __ENV['DEFAULT_ACCOUNT_ID_NFTS_ALLOWANCE_SPENDER'],
    DEFAULT_ACCOUNT_ID_AIRDROP_SENDER: __ENV['DEFAULT_ACCOUNT_ID_AIRDROP_SENDER'],
    DEFAULT_ACCOUNT_ID_AIRDROP_RECEIVER: __ENV['DEFAULT_ACCOUNT_ID_AIRDROP_RECEIVER'],
    DEFAULT_TOPIC_ID: __ENV['DEFAULT_TOPIC_ID'],
    DEFAULT_LIMIT: __ENV['DEFAULT_LIMIT'],
  };
  console.info(`Test parameters - ${JSON.stringify(testParameters, null, '\t')}`);
  return testParameters;
};

export {setupTestParameters};
