const User = require("../models/user.model");

// Register
module.exports.register = async (req, res) => {
  console.log("------------->", req.body);

  try {
    const { username, password, email } = req.body;

    const existingUser = await User.findOne({ username });
    if (existingUser) {
      return res.status(400).json({ message: "User already exists" });
    }

    const newUser = new User({ username, password, email });
    await newUser.save();

    res.status(201).json({ message: "User registered successfully" });
  } catch (err) {
    console.log("--> error", err);
    res.status(500).json({ error: err.message });
  }
};

// Login
module.exports.login = async (req, res) => {
    console.log('--------->',req.body);
    
  try {
    const { email, password, username } = req.body;

    const user = await User.findOne({ email });
    console.log('user',user);
    
    if (!user) return res.status(400).json({ message: "Invalid credentials" });

    if (user.password !== password) {
      return res.status(400).json({ message: "Invalid credentials" });
    }

    res.status(200).json({ message: "Login successful" });
  } catch (err) {
    console.log("--> error", err);
    res.status(500).json({ error: err.message });
  }
};
