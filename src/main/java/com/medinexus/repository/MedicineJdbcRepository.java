package com.medinexus.repository;

import com.medinexus.config.DatabaseConnection;
import com.medinexus.model.Medicine;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MedicineJdbcRepository {

    // ---------------------------------------------------------
    // FIND ALL MEDICINES
    // ---------------------------------------------------------

    public List<Medicine> findAll() {

        List<Medicine> medicines = new ArrayList<>();

        String sql = """
                SELECT medicine_id,
                       name,
                       category,
                       manufacturer,
                       unit_price,
                       quantity,
                       reorder_level,
                       available
                FROM medicines
                ORDER BY medicine_id
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet resultSet =
                     statement.executeQuery()) {

            while (resultSet.next()) {
                medicines.add(mapMedicine(resultSet));
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to fetch medicines",
                    e
            );
        }

        return medicines;
    }

    // ---------------------------------------------------------
    // FIND MEDICINE BY ID
    // ---------------------------------------------------------

    public Medicine findById(int medicineId) {

        String sql = """
                SELECT medicine_id,
                       name,
                       category,
                       manufacturer,
                       unit_price,
                       quantity,
                       reorder_level,
                       available
                FROM medicines
                WHERE medicine_id = ?
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, medicineId);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                if (resultSet.next()) {
                    return mapMedicine(resultSet);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to find medicine " + medicineId,
                    e
            );
        }

        return null;
    }

    // ---------------------------------------------------------
    // FIND MEDICINE BY NAME
    // ---------------------------------------------------------

    public Medicine findByName(String name) {

        if (name == null) {
            return null;
        }

        String sql = """
                SELECT medicine_id,
                       name,
                       category,
                       manufacturer,
                       unit_price,
                       quantity,
                       reorder_level,
                       available
                FROM medicines
                WHERE LOWER(name) = LOWER(?)
                LIMIT 1
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, name.trim());

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                if (resultSet.next()) {
                    return mapMedicine(resultSet);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to find medicine by name",
                    e
            );
        }

        return null;
    }

    // ---------------------------------------------------------
    // ADD MEDICINE
    // ---------------------------------------------------------

    public void add(Medicine medicine) {

        if (medicine == null) {
            return;
        }

        String sql = """
                INSERT INTO medicines (
                    medicine_id,
                    name,
                    category,
                    manufacturer,
                    unit_price,
                    quantity,
                    reorder_level,
                    available
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(
                    1,
                    medicine.getMedicineId()
            );

            statement.setString(
                    2,
                    medicine.getName()
            );

            statement.setString(
                    3,
                    medicine.getCategory()
            );

            statement.setString(
                    4,
                    medicine.getManufacturer()
            );

            statement.setDouble(
                    5,
                    medicine.getUnitPrice()
            );

            statement.setInt(
                    6,
                    medicine.getQuantity()
            );

            statement.setInt(
                    7,
                    medicine.getReorderLevel()
            );

            statement.setBoolean(
                    8,
                    medicine.isAvailable()
            );

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to add medicine",
                    e
            );
        }
    }

    // ---------------------------------------------------------
    // UPDATE MEDICINE
    // ---------------------------------------------------------

    public void update(Medicine medicine) {

        if (medicine == null) {
            return;
        }

        String sql = """
                UPDATE medicines
                SET name = ?,
                    category = ?,
                    manufacturer = ?,
                    unit_price = ?,
                    quantity = ?,
                    reorder_level = ?,
                    available = ?
                WHERE medicine_id = ?
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(
                    1,
                    medicine.getName()
            );

            statement.setString(
                    2,
                    medicine.getCategory()
            );

            statement.setString(
                    3,
                    medicine.getManufacturer()
            );

            statement.setDouble(
                    4,
                    medicine.getUnitPrice()
            );

            statement.setInt(
                    5,
                    medicine.getQuantity()
            );

            statement.setInt(
                    6,
                    medicine.getReorderLevel()
            );

            statement.setBoolean(
                    7,
                    medicine.isAvailable()
            );

            statement.setInt(
                    8,
                    medicine.getMedicineId()
            );

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to update medicine "
                            + medicine.getMedicineId(),
                    e
            );
        }
    }

    // ---------------------------------------------------------
    // REDUCE STOCK ATOMICALLY
    // ---------------------------------------------------------

    /**
     * Reduces medicine stock only when enough stock exists.
     *
     * This operation is atomic at the database level.
     *
     * Example:
     *
     * Stock = 10
     * Requested = 4
     * Result = 6
     *
     * If stock is less than the requested quantity,
     * no row is updated and false is returned.
     */
    public boolean reduceStock(
            int medicineId,
            int quantity) {

        if (medicineId <= 0 || quantity <= 0) {
            return false;
        }

        String sql = """
                UPDATE medicines
                SET quantity = quantity - ?,
                    available = (quantity > 0)
                WHERE medicine_id = ?
                  AND quantity >= ?
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, quantity);
            statement.setInt(2, medicineId);
            statement.setInt(3, quantity);

            return statement.executeUpdate() == 1;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to reduce stock for medicine "
                            + medicineId,
                    e
            );
        }
    }

    // ---------------------------------------------------------
    // DELETE MEDICINE
    // ---------------------------------------------------------

    public void delete(int medicineId) {

        String sql = """
                DELETE FROM medicines
                WHERE medicine_id = ?
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, medicineId);

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to delete medicine "
                            + medicineId,
                    e
            );
        }
    }

    // ---------------------------------------------------------
    // MAP DATABASE ROW TO MEDICINE
    // ---------------------------------------------------------

    private Medicine mapMedicine(ResultSet resultSet)
            throws SQLException {

        return new Medicine(
                resultSet.getInt("medicine_id"),
                resultSet.getString("name"),
                resultSet.getString("category"),
                resultSet.getString("manufacturer"),
                resultSet.getDouble("unit_price"),
                resultSet.getInt("quantity"),
                resultSet.getInt("reorder_level"),
                resultSet.getBoolean("available")
        );
    }
}