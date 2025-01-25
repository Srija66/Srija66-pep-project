package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Model.Account;
import Util.ConnectionUtil;


public class AccountDAO implements SuperDAO<Account> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccountDAO.class);

    /**
     * @param e
     * @param sql
     * @param errormessage
     */
    private void handleSQLException(SQLException e,String sql, String errormessage){
        LOGGER.error("SQL Exception:{ }", e.getMessage());
        LOGGER.error("Sql state:{ }",e.getSQLState());
        LOGGER.error("error code:{ }",e.getErrorCode());
        LOGGER.error("SQL:{ }", sql);
        throw new DAOException(errormessage,e);


    }
    
    

    @Override
    public Optional<Account> getById(int id) {
        // TODO Auto-generated method stub
        String sql="Select * from account where account_id=?";
        
        Connection connection=ConnectionUtil.getConnection();
        try(PreparedStatement ps=connection.prepareStatement(sql)){
            ps.setInt(1, id);
            try(ResultSet rs=ps.executeQuery()){
                if(rs.next()){
                    return Optional.of(new Account(
                        rs.getInt("account_id"),
                        rs.getString("username"),
                        rs.getString("password")));
                    
                } 

            }

        }catch(SQLException e){
            handleSQLException(e, sql, "Error while getting acount with id: "+id);
        }
        return Optional.empty();

    }

    @Override
    public List<Account> getAll() {
        
        List<Account> accounts=new ArrayList<>();
        String sql="Select * from account";
        Connection conn=ConnectionUtil.getConnection();
        try(PreparedStatement ps=conn.prepareStatement(sql)){
            try(ResultSet rs=ps.executeQuery()){
            while(rs.next()){
                Account account=new Account(
                    rs.getInt("account_id"),
                    rs.getString("username"),
                    rs.getString("password"));
                    
                accounts.add(account);
            }
            }
        
        }catch(SQLException e){
            handleSQLException(e, sql, "Error while getting all accounts");
        }
        return accounts;

    }

    public Optional<Account> findAccountByUsername(String username){
        String sql="Select * from account where username=?";
        Connection conn=ConnectionUtil.getConnection();
        try(PreparedStatement ps=conn.prepareStatement(sql)){
            ps.setString(1, username);
            try(ResultSet rs=ps.executeQuery()){
                if(rs.next()){
                    return Optional.of( new Account(
                    rs.getInt("account_id"),
                    rs.getString("username"),
                    rs.getString("password")));
                }
            }

        }catch(SQLException e){
            handleSQLException(e,sql,"error while getting details using name"+username);
        }
        return Optional.empty() ;

    }
    public Optional<Account> validateLogin(String username, String password){
        String sql="Select * from account where username=?";
        Connection conn=ConnectionUtil.getConnection();
        try(PreparedStatement ps=conn.prepareStatement(sql)){
            ps.setString(1, username);
            try(ResultSet rs=ps.executeQuery()){
                if(rs.next()){
                    Account account= new Account(
                    rs.getInt("account_id"),
                    rs.getString("username"),
                    rs.getString("password"));
                    if(Objects.equals(password,account.getPassword())){
                        return Optional.of(account);
                    }
                }
            }

        }catch(SQLException e){
            handleSQLException(e,sql,"error while getting details using name"+username);
        }
        return Optional.empty() ;

    }

    public boolean doesUsernameExists(String username){
        String sql="Select count(*) from account where username=?";
        Connection conn=ConnectionUtil.getConnection();
        try(PreparedStatement ps=conn.prepareStatement(sql)){
            ps.setString(1, username);
            try(ResultSet rs=ps.executeQuery()){
                if(rs.next()){
                    return rs.getInt(1)>0;
                    
                }
            }catch(SQLException e){
            handleSQLException(e,sql,"error while getting details using name"+username);
            }   
        }catch(SQLException e){
            handleSQLException(e, sql, "error while connection");
        }   
        return false ;

    }

    @Override
    public Account insert(Account account) {
        
        String sql = "INSERT INTO account (username, password) VALUES (?, ?)";
        Connection conn = ConnectionUtil.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, account.getUsername());
            ps.setString(2, account.getPassword());
            ps.executeUpdate();

            
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int generatedAccountId = generatedKeys.getInt(1);
                    return new Account(generatedAccountId, account.getUsername(), account.getPassword());
                } else {
                    throw new DAOException("Creating account failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Creating account failed due to SQL error", e);
        }
        
    }

    @Override
    public boolean update(Account account) {
        
        String sql = "UPDATE account SET username = ?, password = ? WHERE account_id = ?";
        Connection conn = ConnectionUtil.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, account.getUsername());
            ps.setString(2, account.getPassword());
            ps.setInt(3, account.getAccount_id());
            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                return true;
            } else {
                throw new DAOException("Updating account failed, no such account found.");
            }
        } catch (SQLException e) {
            throw new DAOException("Updating account failed due to SQL error", e);
        }
        
    }

    @Override
    public boolean delete(Account account) {
        
        String sql = "DELETE FROM account WHERE account_id = ?";
        Connection conn = ConnectionUtil.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, account.getAccount_id());
            int affectedRows = ps.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            throw new DAOException("Deleting account failed due to SQL error", e);
        }
    } 
}
