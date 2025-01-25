package Service;

import java.util.List;
import java.util.Optional;

import DAO.AccountDAO;
import DAO.DAOException;
import Model.Account;

public class AccountService {
    private AccountDAO accountDAO;
  

    public AccountService(){
        accountDAO=new AccountDAO();
    }
    public AccountService(AccountDAO accountDAO){
        this.accountDAO=accountDAO;
    }
    public Optional<Account> getAccountById(int id) {
      
        try {
            Optional<Account> account = accountDAO.getById(id);
        
            return account;
        } catch (DAOException e) {
            throw new UnauthorizedException("Exception occurred while fetching account", e);
        }
    }
    
    public List<Account> getAllAccounts() {

        try {
            List<Account> accounts = accountDAO.getAll();
         
            return accounts;
        } catch (DAOException e) {
            throw new UnauthorizedException("Exception occurred while fetching accounts", e);
        }
    }

    public Optional<Account> findAccountByUsername(String username) {
       
        try {
            Optional<Account> account = accountDAO.findAccountByUsername(username);
        
            return account;
        } catch (DAOException e) {
            throw new UnauthorizedException("Exception occurred while finding account by username " + username, e);
        }
    }
    public Optional<Account> validateLogin(Account account) {
     
        try {
            Optional<Account> validatedAccount = accountDAO.validateLogin(account.getUsername(),
                    account.getPassword());
       
            return validatedAccount;
        } catch (DAOException e) {
            throw new UnauthorizedException("Exception occurred while validating login", e);
        }
    }

    private void validateAccount(Account account) {
      
        try {

            String username = account.getUsername().trim();
            String password = account.getPassword().trim();

            if (username.isEmpty()) {
                throw new UnauthorizedException("Username cannot be blank");
            }
            if (password.isEmpty()) {
                throw new UnauthorizedException("Password cannot be empty");
            }

            if (password.length() < 4) {
                throw new UnauthorizedException("Password must be at least 4 characters long");
            }
            if (accountDAO.doesUsernameExists(account.getUsername())) {
                throw new UnauthorizedException("The username must be unique");
            }
        } catch (DAOException e) {
            throw new UnauthorizedException("Exception occurred while validating account", e);
        }
    }

    public Account createAccount(Account account) {
    
        try {
            validateAccount(account);
            Optional<Account> searchedAccount = findAccountByUsername(account.getUsername());
            if (searchedAccount.isPresent()) {
                throw new UnauthorizedException("Account already exist");
            }
            Account createdAccount = accountDAO.insert(account);
      
            return createdAccount;
        } catch (DAOException e) {
            throw new UnauthorizedException("Exception occurred while creating account", e);
        }
    }
    public boolean updateAccount(Account account) {
        try {
            account.setPassword(account.password);
            boolean updated = accountDAO.update(account);
       
            return updated;
        } catch (DAOException e) {
            throw new UnauthorizedException("Exception occurred while while updating account", e);
        }
    }

    public boolean deleteAccount(Account account) {
      
        if (account.getAccount_id() == 0) {
            throw new IllegalArgumentException("Account ID cannot be null");
        }
        try {
            boolean deleted = accountDAO.delete(account);

            return deleted;
        } catch (DAOException e) {
            throw new UnauthorizedException("Exception occurred while while deleting account", e);
        }
    }

    public boolean accountExists(int accountId) {
     
        try {
            Optional<Account> account = accountDAO.getById(accountId);
            boolean exists = account.isPresent();
       
            return exists;
        } catch (DAOException e) {
            throw new UnauthorizedException("Exception occurred while checking account existence", e);
        }
    }
}
