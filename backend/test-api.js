const axios = require('axios');
const FormData = require('form-data');
const fs = require('fs');
const path = require('path');

const BASE_URL = 'http://localhost:3000/api';
const TEST_IMAGE_PATH = 'C:\\Users\\hp victus\\.gemini\\antigravity\\brain\\6594b77d-7724-4fb2-b3b9-2ae7e486fe13\\test_image_1776788364714.png';

async function test() {
  try {
    const username = 'tester' + Math.floor(Math.random() * 10000);
    const email = `${username}@example.com`;
    const password = 'password123';

    console.log(`1. Registering user: ${username}...`);
    const regRes = await axios.post(`${BASE_URL}/auth/register`, { username, email, password });
    console.log('Registration Response:', regRes.data);

    console.log('2. Logging in...');
    const res = await axios.post(`${BASE_URL}/auth/login`, { email, password });
    const token = res.data.token;
    console.log('Login successful, token received.');

    console.log('3. Uploading image and creating post...');
    const form = new FormData();
    form.append('image', fs.createReadStream(TEST_IMAGE_PATH));
    form.append('caption', 'Automated Test Post: All features verified! 🚀');

    const postRes = await axios.post(`${BASE_URL}/posts/create`, form, {
      headers: {
        ...form.getHeaders(),
        Authorization: `Bearer ${token}`
      }
    });

    console.log('✅ Post created successfully!');
    console.log('Post Image URL:', postRes.data.image);
    console.log('Post Caption:', postRes.data.caption);

  } catch (err) {
    console.error('❌ Test failed:', err.response ? JSON.stringify(err.response.data, null, 2) : err.message);
  }
}

test();
