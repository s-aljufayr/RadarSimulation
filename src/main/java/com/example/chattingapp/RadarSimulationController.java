package com.example.chattingapp;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class RadarSimulationController {

    ObservableList<TrackModel> tracksList;
    UDPSender udpSender = new UDPSender();
    private int minute;
    private int hour;
    private int second;
    static boolean isReachEndLatitude;
    static boolean isReachEndLongitude;
    static boolean isReachEndAltitude;
    static boolean isTrackExists;
    String trackTime;
    @FXML
    private TextField trackFrequencyField;
    @FXML
    private TextField altitudeChangeField;
    @FXML
    private TextField autoTrackEndAltitudeField;
    @FXML
    private TextField autoTrackEndLatitudeField;
    @FXML
    private TextField autoTrackEndLongitudeField;
    @FXML
    private TextField autoTrackIdField;
    @FXML
    private TextField autoTrackSpeedField;
    @FXML
    private TextField autoTrackStartAltitudeField;
    @FXML
    private TextField autoTrackStartLatitudeField;
    @FXML
    private TextField autoTrackStartLongitudeField;
    @FXML
    private TextField latitudeChangeField;
    @FXML
    private TextField longitudeChangeField;
    @FXML
    private TableView<TrackModel> trackTable;
    @FXML
    TableColumn<TrackModel, Double> speedColumn;
    @FXML
    private TableColumn<TrackModel, String> militarySymbolColumn;
    @FXML
    private TableColumn<TrackModel, Double> pV1Column;
    @FXML
    private TableColumn<TrackModel, Double> pV2Column;
    @FXML
    private TableColumn<TrackModel, Integer> radarIdColumn;
    @FXML
    private TableColumn<TrackModel, Double> rcsColumn;
    @FXML
    private TableColumn<TrackModel, Integer> timeColumn;
    @FXML
    private TableColumn<TrackModel, Integer> typeColumn;
    @FXML
    private TableColumn<TrackModel, Double> v1Column;
    @FXML
    private TableColumn<TrackModel, Double> v2Column;
    @FXML
    private TableColumn<TrackModel, Double> enemyLongitudeColumn, endLongitudeColumn, changeInLongitudeColumn;
    @FXML
    private TableColumn<TrackModel, Integer> enemyIdColumn;
    @FXML
    private TableColumn<TrackModel, Double> startAltitudeColumn, startLatitudeColumn, startLongitudeColumn;
    @FXML
    private TableColumn<TrackModel, Double> enemyLatitudeColumn, endLatitudeColumn, changeInLatitudeColumn;
    @FXML
    private TableColumn<TrackModel, Double> enemyAltitudeColumn, endAltitudeColumn, changeInAltitudeColumn;
    /////////////////////////////////////////////////////////
    @FXML
    private TextField deviceAltitudeField;
    @FXML
    private Label deviceAltitudeLable;
    @FXML
    private TextField deviceIdField;
    @FXML
    private Label deviceIdLable;
    @FXML
    private TextField deviceLatitudeField;
    @FXML
    private Label deviceLatitudeLable;
    @FXML
    private TextField deviceLongitudeField;
    @FXML
    private Label deviceLongitudeLable;
    @FXML
    private TextField ipAddressField;
    @FXML
    private TextField portField;
    @FXML
    private Label ipAddressLabel;
    @FXML
    private Label portLabel;

    public RadarSimulationController() throws UnknownHostException {
    }

    @FXML
    void deleteTrackButton(ActionEvent event) {
        int selectedEnemy = trackTable.getSelectionModel().getSelectedIndex();
        trackTable.getItems().remove(selectedEnemy);
    }
    @FXML
    void resetSimulationButton(ActionEvent event) {
        trackTable.getItems().clear();
    }
    @FXML
    void newTrackButton(ActionEvent event) {
        TrackModel track = this.getTrackFromUi();
        this.updateTable(track);
    }
    @FXML
    void startSimulationButton(ActionEvent event) throws IOException {

        List<TrackModel> oneMoveTrackList = new ArrayList<>();

        // this counter to now the #round on the loop
        int firstRound = 0;
        isReachEndLatitude = false;
        isReachEndLongitude = false;
        isReachEndAltitude = false;
        isTrackExists = false;

        while (!(isReachEndLatitude && isReachEndLongitude && isReachEndAltitude)) {

            tracksList = trackTable.getItems();

            for(int rowIndex = 0; rowIndex < tracksList.size(); rowIndex++){

                TrackModel track = tracksList.get(rowIndex);

                this.getTrackFromTable(track);

                System.out.println(("Row Index : " + rowIndex));
                System.out.println("loop Counter: "+firstRound);

                // Perform loop operations here

                if(firstRound == 0){
                    track.setLatitude(this.countLLA(track.getStartLatitude(),track.getEndLatitude(),track.getChangeInLatitude(),isReachEndLatitude,"latitude"));
                    track.setLongitude(this.countLLA(track.getStartLongitude(), track.getEndLongitude(), track.getChangeInLongitude(),isReachEndLongitude, "longitude"));
                    track.setAltitude(this.countLLA(track.getStartAltitude(), track.getEndAltitude(), track.getChangeInAltitude(),isReachEndAltitude, "altitude"));
                }else{
                    track.setLatitude(this.countLLA(track.getLatitude(),track.getEndLatitude(),track.getChangeInLatitude(),isReachEndLatitude,"latitude"));
                    track.setLongitude(this.countLLA(track.getLongitude(), track.getEndLongitude(), track.getChangeInLongitude(),isReachEndLongitude,"longitude"));
                    track.setAltitude(this.countLLA(track.getAltitude(), track.getEndAltitude(), track.getChangeInAltitude(),isReachEndAltitude,"altitude"));
                }

                // Calculate track time
                trackTime = this.getLocalTime();
                track.setTime(trackTime);
//                this.updateRecord(rowIndex,track);

                // Check the track id on the list or not, will update if  exists, will add new if not
                oneMoveTrackList = this.checkTrackIdExists(oneMoveTrackList,track);
                if(!isTrackExists){
                    oneMoveTrackList.add(track);
                }
                this.checkAllArivedToDestination(track);


                // Delay for TrackFrequency second
                try {
                    Thread.sleep((long) (track.getTrackFrequency()*1000));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            // send the track list
            firstRound++;
            udpSender.sendData(oneMoveTrackList);
            System.out.println(oneMoveTrackList);

        }


    }

    @FXML
    void sendConnectionProperties(ActionEvent event) throws UnknownHostException {
        String ipAddress = ipAddressField.getText();
        int port = Integer.parseInt(portField.getText());
        ipAddressLabel.setText(ipAddress);
        portLabel.setText(String.valueOf(port));
        udpSender.setPort(Integer.parseInt(String.valueOf(port)));
        udpSender.setIp_Address(ipAddress);

    }
    @FXML
    void sendDeviceButton(ActionEvent event) throws IOException {
        // get the radar from the UI
        String radarId = deviceIdField.getText();
        String radarLatitude = deviceLatitudeField.getText();
        String radarLongitude = deviceLongitudeField.getText();
        String radarAltitude = deviceAltitudeField.getText();
        RadarModel radar = new RadarModel();
        radar.setId(Integer.parseInt(deviceIdField.getText()));
        radar.setLatitude(Double.parseDouble(deviceLatitudeField.getText()));
        radar.setLongitude(Double.parseDouble(deviceLongitudeField.getText()));
        radar.setAltitude(Double.parseDouble(deviceAltitudeField.getText()));
        // print the radar information on radar labels
        deviceIdLable.setText(radarId);
        deviceLatitudeLable.setText(radarLatitude);
        deviceLongitudeLable.setText(radarLongitude);
        deviceAltitudeLable.setText(radarAltitude);
        // to send the radar information
        udpSender.sendData(radar);

    }
    @FXML
    public void initialize() {

        militarySymbolColumn.setCellValueFactory(new PropertyValueFactory<TrackModel, String>("militarySymbol"));
        pV1Column.setCellValueFactory(new PropertyValueFactory<TrackModel, Double>("p_v1"));
        pV2Column.setCellValueFactory(new PropertyValueFactory<TrackModel, Double>("p_v2"));
        radarIdColumn.setCellValueFactory(new PropertyValueFactory<TrackModel, Integer>("radarId"));
        rcsColumn.setCellValueFactory(new PropertyValueFactory<TrackModel, Double>("rcs"));
        timeColumn.setCellValueFactory(new PropertyValueFactory<TrackModel, Integer>("time"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<TrackModel, Integer>("type"));
        v1Column.setCellValueFactory(new PropertyValueFactory<TrackModel, Double>("v1"));
        v2Column.setCellValueFactory(new PropertyValueFactory<TrackModel, Double>("v2"));
        enemyLongitudeColumn.setCellValueFactory(new PropertyValueFactory<TrackModel, Double>("longitude"));
        enemyIdColumn.setCellValueFactory(new PropertyValueFactory<TrackModel, Integer>("id"));
        enemyLatitudeColumn.setCellValueFactory(new PropertyValueFactory<TrackModel, Double>("latitude"));
        speedColumn.setCellValueFactory(new PropertyValueFactory<TrackModel, Double>("speed"));
        enemyAltitudeColumn.setCellValueFactory(new PropertyValueFactory<TrackModel, Double>("altitude"));
        startLatitudeColumn.setCellValueFactory(new PropertyValueFactory<TrackModel, Double>("startLatitude"));
        startLongitudeColumn.setCellValueFactory(new PropertyValueFactory<TrackModel, Double>("startLongitude"));
        startAltitudeColumn.setCellValueFactory(new PropertyValueFactory<TrackModel, Double>("startAltitude"));
        endLatitudeColumn.setCellValueFactory(new PropertyValueFactory<TrackModel, Double>("endLatitude"));
        endLongitudeColumn.setCellValueFactory(new PropertyValueFactory<TrackModel, Double>("endLongitude"));
        endAltitudeColumn.setCellValueFactory(new PropertyValueFactory<TrackModel, Double>("endAltitude"));
        changeInLatitudeColumn.setCellValueFactory(new PropertyValueFactory<TrackModel, Double>("changeInLatitude"));
        changeInLongitudeColumn.setCellValueFactory(new PropertyValueFactory<TrackModel, Double>("changeInLongitude"));
        changeInAltitudeColumn.setCellValueFactory(new PropertyValueFactory<TrackModel, Double>("changeInAltitude"));

    }
    private String getLocalTime(){

        Calendar cal = Calendar.getInstance();
        second = cal.get(Calendar.SECOND);
        minute = cal.get(Calendar.MINUTE);
        hour = cal.get(Calendar.HOUR);
        String localTime = hour + ":" + (minute) + ":" + second;
        return localTime;

    }
    private Double countLLA(Double geographicCoordinates, Double endLLA,Double changeInLLA, boolean breakLoop, String key){

        if(!breakLoop) {
            if (geographicCoordinates < endLLA) {
                geographicCoordinates += changeInLLA;
                if (geographicCoordinates >= endLLA) {
                    if (key == "latitude") {
                        isReachEndLatitude = true;
                    }
                    if (key == "longitude") {
                        isReachEndLongitude = true;
                    }
                    if (key == "altitude") {
                        isReachEndAltitude = true;
                    }
                }
            }
            else if(geographicCoordinates > endLLA) {
                geographicCoordinates -= changeInLLA;
                if (geographicCoordinates <= endLLA){
                    if(key == "latitude"){
                        isReachEndLatitude = true;
                    }if(key == "longitude"){
                        isReachEndLongitude = true;
                    }if(key == "altitude"){
                        isReachEndAltitude = true;
                    }
                }

            }
            else if(geographicCoordinates == endLLA){
                if(key == "latitude"){
                    isReachEndLatitude = true;
                }if(key == "longitude"){
                    isReachEndLongitude = true;
                }if(key == "altitude"){
                    isReachEndAltitude = true;
                }
            }
        }
        return geographicCoordinates;
    }
    private void getTrackFromTable(TrackModel track){

        track.setId(Integer.parseInt(String.valueOf(enemyIdColumn.getCellData(track))));
        track.setSpeed(Double.parseDouble(String.valueOf(speedColumn.getCellData(track))));
        track.setStartLatitude(Double.parseDouble(String.valueOf(enemyLatitudeColumn.getCellData(track))));
        track.setStartLongitude(Double.parseDouble(String.valueOf(enemyLongitudeColumn.getCellData(track))));
        track.setStartAltitude(Double.parseDouble(String.valueOf(enemyAltitudeColumn.getCellData(track))));
        track.setEndLatitude(Double.parseDouble(String.valueOf(endLatitudeColumn.getCellData(track))));
        track.setEndLongitude(Double.parseDouble(String.valueOf(endLongitudeColumn.getCellData(track))));
        track.setEndAltitude(Double.parseDouble(String.valueOf(endAltitudeColumn.getCellData(track))));
        track.setChangeInLatitude(Double.parseDouble(String.valueOf(changeInLatitudeColumn.getCellData(track))));
        track.setChangeInLongitude(Double.parseDouble(String.valueOf(changeInLongitudeColumn.getCellData(track))));
        track.setChangeInAltitude(Double.parseDouble(String.valueOf(changeInAltitudeColumn.getCellData(track))));
        track.setTrackFrequency(Long.parseLong(trackFrequencyField.getText()));

    }
    private TrackModel getTrackFromUi(){

        TrackModel track = new TrackModel();
        track.setId(Integer.parseInt(autoTrackIdField.getText()));
        track.setSpeed(Double.parseDouble(autoTrackSpeedField.getText()));
        track.setTime(this.getLocalTime());
        track.setLatitude(Double.parseDouble(autoTrackStartLatitudeField.getText()));
        track.setLongitude(Double.parseDouble(autoTrackStartLongitudeField.getText()));
        track.setAltitude(Double.parseDouble(autoTrackStartAltitudeField.getText()));
        track.setStartLatitude(Double.parseDouble(autoTrackStartLatitudeField.getText()));
        track.setStartLongitude(Double.parseDouble(autoTrackStartLongitudeField.getText()));
        track.setStartAltitude(Double.parseDouble(autoTrackStartAltitudeField.getText()));
        track.setEndLatitude(Double.parseDouble(autoTrackEndLatitudeField.getText()));
        track.setEndLongitude(Double.parseDouble(autoTrackEndLongitudeField.getText()));
        track.setEndAltitude(Double.parseDouble(autoTrackEndAltitudeField.getText()));
        track.setChangeInLatitude(Double.parseDouble(latitudeChangeField.getText()));
        track.setChangeInLongitude(Double.parseDouble(longitudeChangeField.getText()));
        track.setChangeInAltitude(Double.parseDouble(altitudeChangeField.getText()));

        return track;

    }
    private void updateTable(TrackModel track){
        tracksList = trackTable.getItems();
        tracksList.add(track);
        trackTable.setItems(tracksList);
    }
    private void checkAllArivedToDestination(TrackModel track) {
        if(isReachEndLatitude && isReachEndLongitude && isReachEndAltitude){

            isReachEndLatitude = track.getLatitude() == track.getEndLatitude() || Math.abs(track.getLatitude() - track.getEndLatitude()) == Math.abs(track.getChangeInLatitude());
            isReachEndLongitude = track.getLongitude() == track.getEndLongitude() || Math.abs(track.getLongitude() - track.getEndLongitude()) == Math.abs(track.getChangeInLongitude());
            isReachEndAltitude = track.getAltitude() == track.getEndAltitude() || Math.abs(track.getAltitude() - track.getEndAltitude()) == Math.abs(track.getChangeInAltitude());
        }
    }
    private List<TrackModel> checkTrackIdExists(List<TrackModel> trackingList, TrackModel track) {
        for(int i = 0; i < trackingList.size(); i++){
            TrackModel ExistingTrack = trackingList.get(i);
            if(ExistingTrack.getId() == track.getId()){
                trackingList.remove(i);
                trackingList.add(track);
                this.isTrackExists = true;
                break;
            }
        }
        return trackingList;
    }
}
