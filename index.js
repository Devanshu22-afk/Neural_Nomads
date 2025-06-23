require('dotenv').config();
console.log('TWILIO_ACCOUNT_SID:', process.env.TWILIO_ACCOUNT_SID);
console.log('ENV FILE PATH:', require('path').resolve('.env'));
// ... existing code ... 