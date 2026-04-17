const express = require("express");
const cors = require("cors");
require('dotenv').config();

const app = express();


app.get("/", (req, res) => {
  res.send("Hello from Express!");
});

app.get("/apiText", (req, res) => {
  res.send("text api for kotlin");
});

app.listen(process.env.PORT, () => {
  console.log(`Server running on port ${process.env.PORT} - `);
});
