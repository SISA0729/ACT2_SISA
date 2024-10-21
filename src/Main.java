import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Main {

    public static void main(String[] args) {
        conectarYCrearUsuarioYBaseDeDatos();
    }

    public static void conectarYCrearUsuarioYBaseDeDatos() {
        String adminUrl = "jdbc:mysql://localhost:3306/";
        String adminUser = "root";
        String adminPassword = "";

        String newUser = "DAM2";
        String newPassword = "DAM2";
        String dbName = "BD_SISA";

        String createUserQuery = "CREATE USER IF NOT EXISTS '" + newUser + "'@'localhost' IDENTIFIED BY '" + newPassword + "'";
        String grantPrivilegesQuery = "GRANT ALL PRIVILEGES ON " + dbName + ".* TO '" + newUser + "'@'localhost'";
        String flushPrivilegesQuery = "FLUSH PRIVILEGES";
        String createDatabaseQuery = "CREATE DATABASE IF NOT EXISTS " + dbName;

        try (Connection adminConnection = DriverManager.getConnection(adminUrl, adminUser, adminPassword);
             Statement statement = adminConnection.createStatement()) {

            statement.executeUpdate(createDatabaseQuery);
            System.out.println("Base de datos " + dbName + " creada o ya existente.");

            statement.executeUpdate(createUserQuery);
            System.out.println("Usuario DAM2 creado o ya existente.");

            statement.executeUpdate(grantPrivilegesQuery);
            System.out.println("Permisos concedidos a DAM2 sobre la base de datos " + dbName);

            statement.executeUpdate(flushPrivilegesQuery);
            System.out.println("Privilegios aplicados.");

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error durante la creación de la base de datos, usuario o la concesión de permisos.");
            return;
        }

        String userUrl = "jdbc:mysql://localhost:3306/" + dbName;
        try (Connection userConnection = DriverManager.getConnection(userUrl, newUser, newPassword)) {
            System.out.println("Conexión exitosa a la base de datos " + dbName + " como usuario DAM2.");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error al conectarse como usuario DAM2 a la base de datos " + dbName);
        }
    }
}