package mate.jdbc.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import mate.jdbc.exceptions.DataProcessingException;
import mate.jdbc.lib.Dao;
import mate.jdbc.model.Manufacturer;
import mate.jdbc.util.ConnectionUtil;

public class ManufacturerDaoImpl implements  ManufacturerDao {
    @Override
    public List<Manufacturer> getAll() {
        String getAllRequest = "SELECT * FROM manufacturers WHERE is_deleted = false";
        List<Manufacturer> allInfo = new ArrayList<>();
        try (Connection connection = ConnectionUtil.getConnection();
             PreparedStatement getAllInfoStatement = connection.prepareStatement(getAllRequest)) {
            ResultSet resultSet = getAllInfoStatement.executeQuery();
            while(resultSet.next()) {
                allInfo.add(getManufacturer(resultSet));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Can't get info from DB", e);
        }
        return allInfo;

    }
    @Override
    public Manufacturer create(Manufacturer manufacturer) {
        String insertManufacturerRequest =
                "INSERT INTO manufacturers (`name`, `country`) VALUES (?, ?)";
        try (Connection connection = ConnectionUtil.getConnection();
             PreparedStatement createManufacturerStatement = connection
                     .prepareStatement(insertManufacturerRequest,
                             Statement.RETURN_GENERATED_KEYS)) {
            createManufacturerStatement.setString(1, manufacturer.getName());
            createManufacturerStatement.setString(2, manufacturer.getCountry());
            createManufacturerStatement.executeUpdate();
            ResultSet generatedKeys = createManufacturerStatement.getGeneratedKeys();
            if (generatedKeys.next()) {
                Long id = generatedKeys.getObject(1, Long.class);
                manufacturer.setId(id);
            }
        } catch (SQLException e) {
            throw new DataProcessingException("Can't insert manufacturer to DB" + manufacturer, e);
        }
        return manufacturer;
    }

    @Override
    public Optional<Manufacturer> get(Long id) {
        String getRequest = "SELECT * FROM manufacturers WHERE is_deleted = false AND id = ?";
        try (Connection connection = ConnectionUtil.getConnection();
             PreparedStatement createManufacturerStatement = connection
                     .prepareStatement(getRequest)) {
            createManufacturerStatement.setLong(1, id);
            ResultSet resultSet = createManufacturerStatement.executeQuery();
            Manufacturer manufacturer = null;
            if (resultSet.next()) {
                manufacturer = getManufacturer(resultSet);
            }
            return Optional.ofNullable(manufacturer);
        } catch (Exception e) {
            throw new DataProcessingException("Can't found manufacturer in DB. Id = " + id, e);
        }
    }

    @Override
    public Manufacturer update(Manufacturer manufacturer) {
        String updateRequest = "UPDATE manufacturers SET name = ?, country = ?"
                + "WHERE id = ? AND is_deleted = false";
        try (Connection connection = ConnectionUtil.getConnection();
             PreparedStatement createManufacturerStatement = connection
                     .prepareStatement(updateRequest)) {
            createManufacturerStatement.setString(1, manufacturer.getName());
            createManufacturerStatement.setString(2, manufacturer.getCountry());
            createManufacturerStatement.setLong(3, manufacturer.getId());
            createManufacturerStatement.executeUpdate();
            return manufacturer;
        } catch (SQLException e) {
            throw new DataProcessingException("Can't update manufacturer in DB" + manufacturer, e);
        }
    }

    @Override
    public boolean delete(Long id) {
        String deleteRequest = "UPDATE manufacturers SET is_deleted = true WHERE id = ?";
        try (Connection connection = ConnectionUtil.getConnection();
             PreparedStatement createManufacturerStatement = connection
                     .prepareStatement(deleteRequest)) {
            createManufacturerStatement.setLong(1, id);
            return createManufacturerStatement.executeUpdate() >= 0;
        } catch (SQLException e) {
            throw new DataProcessingException("Can't delete manufacturer from DB. ID = " + id, e);
        }
    }

    private Manufacturer getManufacturer(ResultSet resultSet) throws SQLException {
        Manufacturer manufacturer = new Manufacturer();
        Long id = resultSet.getObject("id", Long.class);
        String name = resultSet.getString("name");
        String country = resultSet.getString("country");
        manufacturer.setId(id);
        manufacturer.setName(name);
        manufacturer.setCountry(country);
        return manufacturer;
    }
}