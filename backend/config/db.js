const mongoose = require("mongoose")

const DbConnect = ()=>{
  console.log("Connecting to MongoDB...", process.env.MONGODB_URL);
  mongoose.connect(process.env.MONGODB_URL)
  .then(()=>{console.log("Connected to MongoDB")})
  .catch((err)=>{console.log("Error connecting to MongoDB", err)})
}

module.exports = DbConnect;
