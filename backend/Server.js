const express = require("express");
const cors = require("cors");
const authRoutes = require("./routes/user.route");
const DbConnect = require("./config/db");

require('dotenv').config();

const app = express();
// Connect to DB
DbConnect();
app.use(express.json());

app.get("/", (req, res) => {
  res.send("Hello from Express!");
});

app.get("/apiText", (req, res) => {
  res.send("text api for kotlin");
});

// auth
app.use("/api/auth", authRoutes);

app.listen(process.env.PORT, () => {
  console.log(`Server running on port ${process.env.PORT} - `);
});
