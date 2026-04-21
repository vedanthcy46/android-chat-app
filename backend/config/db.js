const mongoose = require("mongoose");
const logger = require("../utils/logger");

const DbConnect = async () => {
  try {
    if (!process.env.MONGODB_URL) {
      throw new Error("MONGODB_URL environment variable is not set");
    }
    const conn = await mongoose.connect(process.env.MONGODB_URL);
    logger.info(`MongoDB Connected: ${conn.connection.host}`);
  } catch (err) {
    logger.error(`Error connecting to MongoDB: ${err.message}`);
    process.exit(1); // Exit process with failure
  }
};

module.exports = DbConnect;
