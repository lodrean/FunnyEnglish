#!/usr/bin/env node

/**
 * Newman runner for API tests with environment setup
 */

const newman = require('newman');
const path = require('path');

const COLLECTION_PATH = path.join(__dirname, 'funnyenglish-api-collection.json');

// Environment configuration
const environments = {
  local: {
    baseUrl: 'http://localhost:8080'
  },
  test: {
    baseUrl: 'http://localhost:8081'
  },
  staging: {
    baseUrl: 'https://api-staging.funnyenglish.app'
  }
};

const env = process.env.TEST_ENV || 'local';
const environment = environments[env];

if (!environment) {
  console.error(`Unknown environment: ${env}`);
  console.error(`Available: ${Object.keys(environments).join(', ')}`);
  process.exit(1);
}

console.log(`🧪 Running API tests against: ${environment.baseUrl}\n`);

// Run Newman
newman.run({
  collection: require(COLLECTION_PATH),
  environment: {
    name: `FunnyEnglish ${env}`,
    values: [
      { key: 'baseUrl', value: environment.baseUrl, enabled: true },
      { key: 'auth_token', value: '', enabled: true },
      { key: 'category_id', value: '', enabled: true },
      { key: 'test_id', value: '', enabled: true },
      { key: 'question_id', value: '', enabled: true },
      { key: 'answer_id', value: '', enabled: true }
    ]
  },
  reporters: ['cli', 'htmlextra'],
  reporter: {
    htmlextra: {
      export: path.join(__dirname, 'reports', `api-test-report-${env}-${Date.now()}.html`),
      title: `FunnyEnglish API Tests - ${env}`,
      showEnvironmentData: true,
      skipHeaders: ['Authorization']
    }
  },
  timeout: {
    request: 30000,
    script: 30000
  },
  delayRequest: 100, // Small delay between requests
  bail: false // Don't stop on first failure
}, function (err, summary) {
  if (err) {
    console.error('\n❌ Error running tests:', err);
    process.exit(1);
  }
  
  const stats = summary.run.stats;
  const total = stats.tests.total;
  const failed = stats.tests.failed;
  const passed = total - failed;
  
  console.log('\n📊 Test Summary:');
  console.log(`   Total:  ${total}`);
  console.log(`   Passed: ${passed} ✅`);
  console.log(`   Failed: ${failed} ❌`);
  console.log(`   Time:   ${summary.run.timings.completed - summary.run.timings.started}ms`);
  
  if (failed > 0) {
    console.log('\n❌ Some tests failed');
    process.exit(1);
  } else {
    console.log('\n✅ All tests passed!');
    process.exit(0);
  }
});
