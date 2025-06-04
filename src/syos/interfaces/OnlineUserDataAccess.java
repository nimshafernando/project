package syos.interfaces;

import syos.model.OnlineUser;

/**
 * Online User data access interface following Interface Segregation Principle
 */
public interface OnlineUserDataAccess extends DataAccessInterface<OnlineUser, String> {

    /**
     * Register a new user
     * 
     * @param user The user to register
     * @return true if successful, false otherwise
     */
    boolean registerUser(OnlineUser user);

    /**
     * Authenticate user by username and password
     * 
     * @param username The username
     * @param password The password
     * @return true if authenticated, false otherwise
     */
    boolean authenticateUser(String username, String password);

    /**
     * Check if username is taken
     * 
     * @param username The username to check
     * @return true if taken, false otherwise
     */
    boolean isUsernameTaken(String username);

    /**
     * Get user by username
     * 
     * @param username The username
     * @return OnlineUser object if found, null otherwise
     */
    OnlineUser getUserByUsername(String username);

    /**
     * Save a user (basic save operation)
     * 
     * @param user The user to save
     * @return true if save successful, false otherwise
     */
    boolean saveUser(OnlineUser user);
}
