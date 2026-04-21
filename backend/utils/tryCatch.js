/**
 * Wraps an async function to catch any errors and pass them to the next middleware.
 * Eliminates the need for try-catch blocks in every controller.
 */
const tryCatch = (controller) => async (req, res, next) => {
  try {
    await controller(req, res, next);
  } catch (error) {
    next(error);
  }
};

module.exports = tryCatch;
