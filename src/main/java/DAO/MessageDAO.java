package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Model.Message;
import Util.ConnectionUtil;

public class MessageDAO implements SuperDAO<Message> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageDAO.class);

    private void handleSQLException(SQLException e,String sql, String errormessage){
        LOGGER.error("SQL Exception:{ }", e.getMessage());
        LOGGER.error("Sql state:{ }",e.getSQLState());
        LOGGER.error("error code:{ }",e.getErrorCode());
        LOGGER.error("SQL:{ }", sql);
        throw new DAOException(errormessage,e);


    }

    private Message mapResultSetToMessage(ResultSet rs)throws SQLException{
        int messageId=rs.getInt("message_id");
        int postedBy=rs.getInt("posted_by");
        String messageText=rs.getString("message_text");
        long timePostedEpoch=rs.getLong("time_posted_epoch");
        return new Message(messageId,postedBy,messageText,timePostedEpoch);
    }

    private List<Message> mapResultSetToList(ResultSet rs) throws SQLException {
        List<Message> messages = new ArrayList<>();
        while (rs.next()) {
            messages.add(mapResultSetToMessage(rs));
        }
        return messages;
    }

    @Override
    public Optional<Message> getById(int id) {
        
        String sql="Select * from message where message_id=?";
        Connection connection=ConnectionUtil.getConnection();
        try(PreparedStatement ps=connection.prepareStatement(sql)){
            ps.setInt(1, id);
            try(ResultSet rs=ps.executeQuery()){
                if(rs.next()){
                    return Optional.of(mapResultSetToMessage(rs));                       
                    
                } 

            }

        }catch(SQLException e){
            handleSQLException(e, sql, "Error while getting acount with id: "+id);
        }
        return Optional.empty();
    }

    @Override
    public List<Message> getAll() {
        List<Message> msg=new ArrayList<>();
        String sql="Select * from message";
        Connection conn=ConnectionUtil.getConnection();
        try(PreparedStatement ps=conn.prepareStatement(sql)){
            try(ResultSet rs=ps.executeQuery()){
            while(rs.next()){
                msg.add(mapResultSetToMessage(rs));
                
            }
            }
        
        }catch(SQLException e){
            handleSQLException(e, sql, "Error while getting all messages");
        }
        return msg;
        
    }

    public List<Message> getMessagesByAccountId(int accountId) {
        String sql = "SELECT * FROM message WHERE posted_by = ?";
        Connection conn = ConnectionUtil.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                return mapResultSetToList(rs);
            }
        } catch (SQLException e) {
            handleSQLException(e, sql, "Error while retrieving a message by account ID: " + accountId);
        }
        return new ArrayList<>();
    }

    
    @Override
    public Message insert(Message message) {
        
        String sql = "INSERT INTO message (posted_by,message_text,time_posted_epoch) VALUES (?, ?, ?)";
        Connection conn = ConnectionUtil.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, message.getPosted_by());
            ps.setString(2, message.getMessage_text());
            ps.setLong(3,message.getTime_posted_epoch());

            ps.executeUpdate();
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int generatedAccountId = generatedKeys.getInt(1);
                    return new Message(generatedAccountId,message.getPosted_by(),message.getMessage_text(),message.getTime_posted_epoch());
                } else {
                    throw new DAOException("inserting message failed, no ID obtained.");
                }
            }
        
        }catch(SQLException e) {
            handleSQLException(e,sql,"inserting message failed due to SQL error");
        }
        throw new DAOException("failed to insert message");
    }

    @Override
    public boolean update(Message message) {
        
        String sql = "UPDATE message SET posted_by = ?, message_text = ?, time_posted_epoch = ? WHERE message_id = ?";
        int rowsUpdated = 0;
        Connection conn = ConnectionUtil.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, message.getPosted_by());
            ps.setString(2, message.getMessage_text());
            ps.setLong(3, message.getTime_posted_epoch());
            ps.setInt(4, message.getMessage_id());
            rowsUpdated = ps.executeUpdate();
        } catch (SQLException e) {
            handleSQLException(e, sql, "Error while updating the message with id: " + message.getMessage_id());
        }
        return rowsUpdated > 0;
        
    }

    

    @Override
    public boolean delete(Message message) {
    

        String sql = "DELETE FROM message WHERE message_id = ?";
        int rowsUpdated = 0;
        Connection conn = ConnectionUtil.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, message.getMessage_id());
            rowsUpdated = ps.executeUpdate();
        } catch (SQLException e) {
            handleSQLException(e, sql, "Error while deleting the message with id: " + message.getMessage_id());
        }
        return rowsUpdated > 0;
        
    }
    
}
