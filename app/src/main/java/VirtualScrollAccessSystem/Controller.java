package VirtualScrollAccessSystem;

// import java.io.BufferedReader;
import java.io.File;
// import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import org.bson.Document;

import com.mongodb.client.MongoCollection;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.control.TextArea;



// import java.awt.Desktop;
import java.util.Map;
import java.util.HashMap;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;



public class Controller implements Initializable {
    
    private static final MongoCollection<Document> mongoCollectionUserData = Database.getUserProfiles();

    private Stage stage;
    private Parent root;

    //Login
    @FXML
	private TextField loginUsername;
    @FXML
	private TextField loginPassword;
    @FXML
	private Label loginError;

    //SignUp
    @FXML
	private TextField signUpUsername;
    @FXML
	private TextField signUpFullName;
    @FXML
	private TextField signUpEmail;
    @FXML
	private TextField signUpPhoneNumber;
    @FXML
	private TextField signUpPassword;
    @FXML
	private TextField signUpConfirmPassword;
    @FXML
	private TextField signUpType;
    @FXML
	private Label signUpError;

    @FXML
	private Label title;
    @FXML
	private Button myButton;

    //Home
    @FXML
    private ImageView homeProfilePic;
    @FXML
	private Label homeName;
    @FXML
	private Label homeType;
    @FXML
	private Button updateDetailsButton;
    @FXML
	private Button adminPrivilegesButton;
    @FXML
    private Button myScrollButton;

    @FXML
    private TextField byTextField;
    @FXML
    private TextField scrollIdTextField;
    @FXML
    private TextField titleTextField;
    @FXML
    private TextField uploadDateTextField;
    private String[][] searchScrolls;

    @FXML
    private TableView<Map<String, String>> homeScrollTableView;
    @FXML
    private TableColumn<Map<String, String>, String> byTableColumn;
    @FXML
    private TableColumn<Map<String, String>, String> scrollID;
    @FXML
    private TableColumn<Map<String, String>, String> scrollTitle;
    @FXML
    private TableColumn<Map<String, String>, String> uploadDate;
    private String scrollIdValue;

    @FXML
    private Label scrollError;

    @FXML
    private Button downloadButton;

    //ScrollPreview
    @FXML
    private Label previewName;
    @FXML
    private TextArea previewContent;
    @FXML
    private Button previewDownload;

    //UpdateDetails
    @FXML
    private ImageView detailsProfilePic;
    @FXML
    private TextField detailsUsername;
    @FXML
	private TextField detailsFullName;
    @FXML
	private TextField detailsEmail;
    @FXML
	private TextField detailsPhoneNumber;

    @FXML
	private TextField detailsPassword;
    @FXML
	private Label detailsError;

    //UpdatePassword
    @FXML
	private TextField currentPassword;
    @FXML
	private TextField newPassword;
    @FXML
	private TextField confirmNewPassword;
    @FXML
    private Label passwordError;

    //AdminPrivilages
    @FXML
    private ListView<String> usersListView;
    @FXML
    private Label adminError;
    private String selectedUser;

    //AdminPrivilagesUserProfile
    @FXML
    private ImageView userProfilePic;
    @FXML
    private Label userUsername;
    @FXML
    private Label userFullName;
    @FXML
    private Label userEmail;
    @FXML
    private Label userPhoneNumber;
    @FXML
    private Label userScrollDownloadCount;
    @FXML
    private Label userScrollUploadCount;

    //Logs
    @FXML
    private ListView<String> logs;

    //MyScrolls
    @FXML
    private TableView<Map<String, String>> myScrollTableView;
    @FXML
    private TableColumn<Map<String, String>, String> myScrollID;
    @FXML
    private TableColumn<Map<String, String>, String> myScrollTitle;
    @FXML
    private TableColumn<Map<String, String>, String> myUploadDate;
    @FXML
    private TableColumn<Map<String, String>, String> downloadsCount;
    private String myScrollIdValue;

    @FXML
	private Label myScrollError;

    //ScrollView
    @FXML
    private TextField viewName;
    @FXML
    private TextArea viewContent;
    @FXML
    private Label viewError;
    private File viewFile;



    //Switching Scene Functions
    public void switchToLogin(ActionEvent event) throws IOException { 

        root = FXMLLoader.load(getClass().getResource("/login.fxml")); 
        stage = (Stage)((Node)event.getSource()).getScene().getWindow(); 

        stage.setTitle("VSAS Login");
        stage.setScene(new Scene(root));
        stage.show();
    }

    private void switchToSignUp(ActionEvent event) throws IOException { 

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/signUp.fxml"));
        loader.setController(this);
        Parent root = loader.load();
    
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
    
        stage.setTitle("Sign Up");
        stage.setScene(new Scene(root));
        stage.show();
    
        
        if (App.flag) {
            title.setText("Add User");
            myButton.setText("Go Back to Privileges");
        } else {
            title.setText("Sign Up");
            myButton.setText("Already Have an Account");
        }
        
    }

    public void switchToHome(ActionEvent event) throws IOException { 

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/home.fxml"));
        loader.setController(this);
        Parent root = loader.load();

        stage = (Stage)((Node)event.getSource()).getScene().getWindow();

        stage.setTitle("Scroll View");
        stage.setScene(new Scene(root));
        stage.show();


        homeName.setText(App.name);
        homeType.setText(App.type);

        if (App.type.equals("Member")){
            adminPrivilegesButton.setVisible(false);
            adminPrivilegesButton.setManaged(false);
        } 
        else if (App.type.equals("Guest")){/////////////////////////////
            updateDetailsButton.setVisible(false); 
            updateDetailsButton.setManaged(false);
            adminPrivilegesButton.setVisible(false);
            adminPrivilegesButton.setManaged(false);
            myScrollButton.setVisible(false);
            myScrollButton.setManaged(false);
            downloadButton.setVisible(false);
            downloadButton.setManaged(false);
        }

        scrollIdValue = null;

        File file = Login.getProfilePic(App.name);
        
        if (file != null) {
            Image image = new Image(file.toURI().toString());
            homeProfilePic.setImage(image);
        }



        byTableColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().get("0"))
        );
        scrollID.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().get("1"))
        );
        scrollTitle.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().get("2"))
        );
        uploadDate.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().get("3"))
        );
        downloadsCount.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().get("4"))
            );

        if (!App.type.equals("Admin")){
            uploadDate.setPrefWidth(225);
            homeScrollTableView.getColumns().remove(downloadsCount); 
        }

        ObservableList<Map<String, String>> data = FXCollections.observableArrayList();

        String[][] scrolls;
        if(searchScrolls != null){
            scrolls = searchScrolls;
        } else {
            scrolls = Scroll.viewAllScroll();
        }

        for (int i=0; i < scrolls.length; i++){
            data.add(convertArrayToMap(scrolls[i]));
        }

        homeScrollTableView.setItems(data);


        homeScrollTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            scrollIdValue = newSelection.get("1");
        });     
    }

    public void switchToScrollPreview(ActionEvent event) throws IOException { 

        if (scrollIdValue != null){
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/preview.fxml"));
            loader.setController(this);
            Parent root = loader.load();

            stage = (Stage)((Node)event.getSource()).getScene().getWindow();

            stage.setTitle("Scroll View");
            stage.setScene(new Scene(root));
            stage.show();

            
            File file = Scroll.viewScroll(scrollIdValue);

            previewName.setText(Scroll.getFileName(scrollIdValue));

            String content = String.join("\n", Files.readAllLines(file.toPath()));
            
            previewContent.setText(content);

            previewContent.setEditable(false);

            if (App.type.equals("Guest")){
                previewDownload.setVisible(false);
                previewDownload.setManaged(false);
            }

        } else {
            scrollError.setText("Select a file");
        }
    }

    public void switchToUserDetails(ActionEvent event) throws IOException { 

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/userDetails.fxml"));
        loader.setController(this);
        Parent root = loader.load();

        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        
        stage.setTitle("User Details");
        stage.setScene(new Scene(root));
        stage.show();

        File file = Login.getProfilePic(App.name);
        
        if (file != null) {
            Image image = new Image(file.toURI().toString());
            detailsProfilePic.setImage(image);
        }

        String[] details = Login.viewDetails(App.name);

        detailsUsername.setText(App.name);
        detailsFullName.setText(details[0]);
        detailsEmail.setText(details[1]);
        detailsPhoneNumber.setText(details[2]);
    }

    public void switchToPasswordDetails(ActionEvent event) throws IOException { 

        Parent root = FXMLLoader.load(getClass().getResource("/passwordDetails.fxml"));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();

        stage.setTitle("Change Password");
        stage.setScene(new Scene(root));
        stage.show();
    }

    public void switchToAdminPrivileges(ActionEvent event) throws IOException { 

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/adminPrivileges.fxml"));
        loader.setController(this);
        Parent root = loader.load();

        stage = (Stage)((Node)event.getSource()).getScene().getWindow();

        stage.setTitle("Admin Privileges");
        stage.setScene(new Scene(root));
        stage.show();

        selectedUser = null;

        usersListView.getItems().addAll(Admin.ListofUsers(mongoCollectionUserData)); //

        usersListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() { 
            @Override 
            public void changed(ObservableValue<? extends String> arg0, String arg1, String arg2) { 
                selectedUser = usersListView.getSelectionModel().getSelectedItem(); 
                //Label.setText(String);
            }
        });
    }

    public void switchToAdminUserProfiles(ActionEvent event) throws IOException { 
        //adminError.setText("Fetching user's datails");

        if (selectedUser != null){

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/adminUserProfiles.fxml"));
            loader.setController(this);
            Parent root = loader.load();

            stage = (Stage)((Node)event.getSource()).getScene().getWindow();

            stage.setTitle("User Profile");
            stage.setScene(new Scene(root));
            stage.show();


            File file = Login.getProfilePic(selectedUser);
            
            if (file != null) {
                Image image = new Image(file.toURI().toString());
                userProfilePic.setImage(image);
            }

            String[] details = Login.viewDetails(selectedUser);

            userUsername.setText(selectedUser);
            userFullName.setText(details[0]);
            userEmail.setText(details[1]);
            userPhoneNumber.setText(details[2]);
            userScrollDownloadCount.setText(details[4]);
            userScrollUploadCount.setText(details[3]);
        } else {
            adminError.setText("Select a user");
        }
    }

    public void switchToLogs(ActionEvent event) throws IOException { 
        //adminError.setText("Fetching user's datails");

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/logs.fxml"));
        loader.setController(this);
        Parent root = loader.load();

        stage = (Stage)((Node)event.getSource()).getScene().getWindow();

        stage.setTitle("User Profile");
        stage.setScene(new Scene(root));
        stage.show();

        
        logs.getItems().addAll(Admin.logs());
    }

    public void switchToMyScroll(ActionEvent event) throws IOException { 

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/myScroll.fxml"));
        loader.setController(this);
        Parent root = loader.load();

        stage = (Stage)((Node)event.getSource()).getScene().getWindow();

        stage.setTitle("My Scroll");
        stage.setScene(new Scene(root));
        stage.show();

        myScrollIdValue = null;

        myScrollID.setCellValueFactory(cellData -> 
        new SimpleStringProperty(cellData.getValue().get("0"))
        );
        myScrollTitle.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().get("1"))
        );
        myUploadDate.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().get("2"))
        );

        ObservableList<Map<String, String>> data = FXCollections.observableArrayList();

        String[][] myScrolls = Scroll.myScrolls(App.name);

        for (int i=0; i < myScrolls.length; i++){
            data.add(convertArrayToMap(myScrolls[i]));
        }

        myScrollTableView.setItems(data);


        myScrollTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            myScrollIdValue = newSelection.get("0");
        });
    }

    public void switchToScrollView(ActionEvent event) throws IOException { 

        if (myScrollIdValue != null){
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view.fxml"));
            loader.setController(this);
            Parent root = loader.load();

            stage = (Stage)((Node)event.getSource()).getScene().getWindow();

            stage.setTitle("Scroll View");
            stage.setScene(new Scene(root));
            stage.show();


            viewFile = Scroll.viewScroll(myScrollIdValue);

            viewName.setText(Scroll.getFileName(myScrollIdValue));

            String content = String.join("\n", Files.readAllLines(viewFile.toPath()));
            viewContent.setText(content);
            
        } else {
            myScrollError.setText("Select a file");
        }
    }


    // Login
    public void login(ActionEvent event) throws IOException { 
        //loginError.setText("Thank you for your patience");
        String status = Login.userLogin(loginUsername.getText(), loginPassword.getText());
        if (loginUsername.getText() == "admin" && loginPassword.getText() == "admin"){
            App.name = "Admin";
            App.type = "Admin";
            switchToHome(event);
        }
        else if (status.equals("Member") || status.equals("Admin")){
            App.name = loginUsername.getText();
            App.type = status;
            switchToHome(event);
        }
        else{
            loginError.setText(status);
        }
    }
    
    public void signUp(ActionEvent event) throws IOException { 
        App.flag = false;
        switchToSignUp(event);
        App.type = "Member";
    }

    public void guest(ActionEvent event) throws IOException { 
        App.type = "Guest";
        switchToHome(event);
    }

    //SignUp
    public void addUser(ActionEvent event) throws IOException { 
        //signUpError.setText("Making a new account");
        String Status = Login.addUser(signUpUsername.getText(), signUpFullName.getText(), signUpEmail.getText(), signUpPhoneNumber.getText(), "Member", signUpPassword.getText(), signUpConfirmPassword.getText());

        if (Status.equals("Success")){
            if (!App.flag){
            App.name = signUpUsername.getText();
            switchToHome(event);
            } else {
                switchToAdminPrivileges(event);
            }
        } else{
            signUpError.setText(Status);
        }
    }

    //Home
    public void logout(ActionEvent event) throws IOException { 
        App.name = "";
        App.flag = false;
        switchToLogin(event);
    }

    public void searchScrolls(ActionEvent event) throws IOException { 
        String by = byTextField.getText();
        String scrollId = scrollIdTextField.getText();
        String title = titleTextField.getText();
        String uploadDate = uploadDateTextField.getText();

        searchScrolls = Scroll.searchScroll(by, scrollId, title, uploadDate);
        switchToHome(event);

        byTextField.setText(by);
        scrollIdTextField.setText(scrollId);
        titleTextField.setText(title);
        uploadDateTextField.setText(uploadDate);
    }

    private Map<String, String> convertArrayToMap(String[] array) {
        Map<String, String> map = new HashMap<>();

        for (int i = 0; i < array.length; i++) {
            map.put(String.valueOf(i), array[i]);
        }

        return map;
    }

    public void downloadScroll(ActionEvent event) throws IOException { 

        if (scrollIdValue != null){
            File file = Scroll.viewScroll(scrollIdValue);

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save File As");

            fileChooser.setInitialFileName(file.getName());

            fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Files", "*.*")
            );

            
            File fileLocation = fileChooser.showSaveDialog(stage);
            if (fileLocation != null) {
                java.nio.file.Files.copy(file.toPath(), fileLocation.toPath(), StandardCopyOption.REPLACE_EXISTING);
                Scroll.updateDownloadCount(scrollIdValue);
                Scroll.incrementUserDownloadCount(App.name);
                switchToHome(event);
            } else {
                scrollError.setText("Choose a location to download");
            }

        } else {
            scrollError.setText("Select a file");
        }
    }

    // public void previewScroll(ActionEvent event) throws IOException { 
    //     File file = Scroll.viewScroll(scrollIdValue);
    //     if (file != null) {
    //         Desktop desktop = Desktop.getDesktop();
    //         if (desktop.isSupported(Desktop.Action.OPEN)) {
    //             desktop.open(file);
    //         } else {
    //             myScrollError.setText("No default application or Open Action is not supported on this platform.");
    //         }
    //     }
    // }

    //UpdateDetails
    public void updateDetails(ActionEvent event) throws IOException { 
        //detailsError.setText("Updating details");
        String Status = Login.updateDetails(App.name, detailsUsername.getText(), detailsFullName.getText(), detailsEmail.getText(), detailsPhoneNumber.getText(), detailsPassword.getText());
        detailsError.setText(Status);
        if (Status.equals("Success")){
            App.name = detailsUsername.getText();
        }
    }

    public void updateProfilePicture(ActionEvent event) throws IOException { 
        //detailsError.setText("Updating Profile Picture");

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Binary File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));

        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            String Status = Login.saveProfilePic(App.name,file);
            switchToUserDetails(event);
            scrollError.setText(Status);
        } else {
            scrollError.setText("Choose a picture");
        }

        
    }

    //UpdatePassword
    public void updatePassword(ActionEvent event) throws IOException { 
        //passwordError.setText("Updating password");
        String Status = Login.updatePassword(App.name, currentPassword.getText(), newPassword.getText(), confirmNewPassword.getText());
        passwordError.setText(Status);
    }

    //AdminPrivilages  
    public void deleteUser(ActionEvent event) throws IOException { 
        //adminError.setText("Deleting user");
        if (selectedUser != null){
            Admin.DeleteUser(selectedUser);
            usersListView.getItems().remove(selectedUser);
            adminError.setText("Account deleted");
        } else {
            adminError.setText("Select a user");
        }
    }

    public void addUserByAdmin(ActionEvent event) throws IOException { 
        App.flag = true;
        switchToSignUp(event);
    }

    public void adminButton(ActionEvent event) throws IOException {
        if (App.flag) {
            switchToAdminPrivileges(event);
        } else {
            switchToLogin(event);
        }
    }

    //MyScroll
    public void addScroll(ActionEvent event) throws IOException { 

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Binary File");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Text Files", "*.txt") 
        );

        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            String Status = Scroll.addScroll(App.name,file.getName(),file);
            switchToMyScroll(event);
            myScrollError.setText(Status);
        } else{
            myScrollError.setText("Select a file");
        }
    }

    // public void viewScroll(ActionEvent event) throws IOException { 

    //     File file = Scroll.viewScroll(myScrollIdValue);;
    //     if (file != null) {
    //         Desktop desktop = Desktop.getDesktop();
    //         if (desktop.isSupported(Desktop.Action.OPEN)) {
    //             desktop.open(file);
    //         } else {
    //             myScrollError.setText("No default application or Open Action is not supported on this platform.");
    //         }
    //     }
    // }

    public void deleteScroll(ActionEvent event) throws IOException { 
        if (myScrollIdValue != null){
            String Status = Scroll.deleteScroll(myScrollIdValue);
            switchToMyScroll(event);
            myScrollError.setText(Status);
        } else {
            myScrollError.setText("Select a file");
        }
    }

    public void updateScroll(ActionEvent event) throws IOException { 

        String content = viewContent.getText(); //TextArea
        System.out.println(content);
        String name = viewName.getText();  //Label

        try (FileWriter fileWriter = new FileWriter(viewFile)) {
            fileWriter.write(content);
        }
        String Status = Scroll.editScroll(myScrollIdValue, name, viewFile);
        viewError.setText(Status);




    //     FileChooser fileChooser = new FileChooser();
    //     fileChooser.setTitle("Select Binary File");
    //     fileChooser.getExtensionFilters().addAll(
    //             new FileChooser.ExtensionFilter("All Files", "*.*")
    //     );

    //     File file = fileChooser.showOpenDialog(stage);
    //     if (file != null) {
    //         String Status = Scroll.editScroll(myScrollIdValue,file.getName(), file);
    //         scrollError.setText(Status);
    //     }

    //     switchToMyScroll(event);
    }

    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // TODO Auto-generated method stub
        
    }

}