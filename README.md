# Smart Hospital Management System — Frontend

A polished, responsive HTML/CSS/JavaScript prototype for the proposed SHMS desktop application.

## Modules
- Role-aware Admin dashboard
- Patient registration and search
- Appointment management
- Severity-based smart queue visualization
- Doctor availability
- Electronic prescriptions
- Billing/invoices
- Medicine inventory
- Modal forms and notifications

## JavaFX conversion map
| Web prototype | JavaFX |
|---|---|
| Sidebar navigation | `VBox` + `Button` |
| Cards | `HBox` / `VBox` + CSS |
| Tables | `TableView` |
| Forms | `TextField`, `ComboBox`, `DatePicker` |
| Modal | `Dialog` |
| Dashboard sections | `BorderPane` / `StackPane` |
| Priority queue | `TableView` backed by `ObservableList` |
| Toast | custom `Popup` / `StackPane` |
| CSS theme | JavaFX `.css` stylesheet |

The Java version should move data, validation, authentication, Strategy/Observer logic, and JDBC/MySQL access into separate service/repository layers.
