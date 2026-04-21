const logger = require('../utils/logger');

/**
 * Centralized Error Handling Middleware
 * Catch all errors passed to next() and return a consistent JSON response.
 */
const errorHandler = (err, req, res, next) => {
  // Set default status code if not already set
  let statusCode = res.statusCode === 200 ? 500 : res.statusCode;
  
  // Specific handling for known error types (e.g., Mongoose validation errors)
  if (err.name === 'ValidationError') statusCode = 400;
  if (err.name === 'CastError') statusCode = 404;

  logger.error(`[${req.method}] ${req.url} - ${err.message}`);

  res.status(statusCode).json({
    success: false,
    message: err.message || 'Internal Server Error',
    stack: process.env.NODE_ENV === 'production' ? null : err.stack,
  });
};

module.exports = errorHandler;
