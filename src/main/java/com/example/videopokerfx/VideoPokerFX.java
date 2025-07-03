/**
 * Täname autoreid, kes on lubanud oma loomingut kasutada:
 * dallas (itch.io) - kaardipakk
 * floraphonic (pixaby.com) - heliefektid
 */

package com.example.videopokerfx;

import javafx.application.Application;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.media.AudioClip;
import javafx.stage.Stage;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class VideoPokerFX extends Application {

    private double balanss = 500;
    private double panus = 0;
    private String käsi = "";
    private double võit = 0;
    private int mituKordaMängitud = 0;
    private final Label balanssLabel = new Label("Balanss: 500€");
    private final Label tulemusLabel = new Label();
    private final Label kaedLabel = new Label();
    private final Label mituKordaLabel = new Label();
    private final TextField panusField = new TextField("15");

    private final Button mängiButton = new Button("MÄNGI");
    BooleanProperty kasKaardidJagatud = new SimpleBooleanProperty(false);

    private enum MänguTegevus{
        START,
        MÄNGU_TULEMUS,
        ERROR
    }

    // Heliefektid
    AudioClip win = new AudioClip(getClass().getResource("/sounds/win.mp3").toExternalForm());
    AudioClip loss = new AudioClip(getClass().getResource("/sounds/loss.mp3").toExternalForm());
    AudioClip select = new AudioClip(getClass().getResource("/sounds/select.mp3").toExternalForm());
    AudioClip unselect = new AudioClip(getClass().getResource("/sounds/unselect.mp3").toExternalForm());
    AudioClip generate = new AudioClip(getClass().getResource("/sounds/generate.mp3").toExternalForm());

    private final List<ToggleButton> hoitudNupud = new ArrayList<>();
    private final List<ImageView> kaartViewid = new ArrayList<>();
    private final KaartideGeneraator generaator = new KaartideGeneraator();
    private List<Kaart> kaardid = new ArrayList<>();
    private final TextArea koefitsendid = new TextArea("""
    Royal Flush: 800 × panus
    Straight Flush: 50 × panus
    Four of a Kind: 30 × panus
    Full House: 10 × panus
    Flush: 8 × panus
    Straight: 5 × panus
    Three of a Kind: 3 × panus
    Two Pair: 1.5 × panus
    Jacks or Better: 1 × panus
    One Pair: 0.5 × panus
    High Card: 0
    """);

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage peaLava) {

        kirjutaLogisse(MänguTegevus.START);

        // Tagatausta määramine
        BackgroundImage taust = new BackgroundImage(
                new Image(getClass().getResource("/images/poker_table.jpg").toExternalForm()),
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, true, true)
        );

        // Leia, mitu korda on varem mängitud
        try (BufferedReader lugeja = new BufferedReader(new InputStreamReader(new FileInputStream("data/howmanytimesplayed.txt")))){
            mituKordaMängitud = Integer.parseInt(lugeja.readLine().trim());
        } catch (IOException e) {
            System.err.println("Ei leidnud faili 'howmanytimesplayed.txt'");
            kirjutaLogisse(MänguTegevus.ERROR, "Ei leidnud faili 'howmanytimesplayed.txt'");
        }

        mituKordaLabel.setText("Mitu korda mängitud: " + mituKordaMängitud);

        // Välimuse määramine
        String labelStyle = "-fx-text-fill: white; -fx-font-size: 15; -fx-font-weight: bold;";
        koefitsendid.setEditable(false);
        balanssLabel.setStyle(labelStyle);
        tulemusLabel.setStyle(labelStyle);
        kaedLabel.setStyle(labelStyle);
        mituKordaLabel.setStyle(labelStyle);
        panusField.setStyle("-fx-control-inner-background: white; -fx-text-fill: black;");
        panusField.setMinHeight(15);
        VBox root = new VBox(15);
        root.setPadding(new Insets(15));
        root.setAlignment(Pos.CENTER);

        root.setBackground(new Background(taust));

        // Ülemine info
        HBox koefitsnedid = new HBox(40, koefitsendid);
        koefitsnedid.setAlignment(Pos.TOP_CENTER);
        HBox ylemineRida = new HBox(20, balanssLabel, mituKordaLabel);
        ylemineRida.setAlignment(Pos.CENTER);

        // Keskmine kaartide osa
        HBox kaartideRida = new HBox(10);
        kaartideRida.setAlignment(Pos.CENTER);

        // 5 kaardi koos "HOIA" nupuga genereerimine
        for (int i = 0; i < 5; i++) {
            ToggleButton hoiaNupp = new ToggleButton("HOIA");
            hoiaNupp.setMaxWidth(80);
            hoiaNupp.setOnAction(e -> {
                if (hoiaNupp.isSelected()) {
                    select.play();
                } else {
                    unselect.play();
                }
            });
            hoiaNupp.getStyleClass().add("hoia-nupp");
            hoiaNupp.setDisable(true);
            hoitudNupud.add(hoiaNupp);


            ImageView kaartView = new ImageView();
            kaartView.setFitWidth(80);
            kaartView.setFitHeight(120);
            kaartViewid.add(kaartView);

            VBox kaartBlokk = new VBox(5, hoiaNupp, kaartView);
            kaartBlokk.setAlignment(Pos.CENTER);
            kaartViewid.get(i).setImage(
                    new Image(Kaart.class.getResource("/images/green_backing.png").toExternalForm())
            );
            kaartideRida.getChildren().add(kaartBlokk);
        }

        // Alumine rida
        Label panusLabel = new Label("Panus: ");
        panusLabel.setStyle(labelStyle);
        HBox alumineRida = new HBox(15,
                panusLabel, panusField, mängiButton);
        alumineRida.setAlignment(Pos.CENTER);

        root.getChildren().addAll(koefitsnedid, ylemineRida, kaartideRida, alumineRida, kaedLabel, tulemusLabel);

        mängiButton.getStyleClass().add("mangi-nupp");
        mängiButton.setOnAction(e -> {
            if (!kasKaardidJagatud.get()){
                jagaKaardid();
            }else {
                vahetaKaardid();
            }
        });

        Scene stseen = new Scene(root, 700, 450);
        stseen.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        peaLava.setTitle("VideoPoker FX");
        peaLava.setScene(stseen);
        peaLava.show();
    }

    private void jagaKaardid() {
        // Proovime teha panusest double väärtuse
        try {
            panus = Double.parseDouble(panusField.getText());
            if (panus <= 0 || panus > balanss) {
                tulemusLabel.setText("Vale panus!");
                return;
            }
        } catch (Exception e) {
            tulemusLabel.setText("Sisesta korrektne panus!");
            kirjutaLogisse(MänguTegevus.ERROR, "Kasutaja sisestas vale panuse: '" + panusField.getText() + "'");
            return;
        }

        // Lahutame maha balansist ning genereerime kaardid
        balanss -= panus;
        uuendaBalanssi();
        generate.play();
        mituKordaMängitud++;
        uuendaMituKorda(mituKordaMängitud);

        kaardid = generaator.genereerimeKaardid(5);
        for (int i = 0; i < 5; i++) {
            hoitudNupud.get(i).setSelected(false);
            hoitudNupud.get(i).setDisable(false);
            kaartViewid.get(i).setImage(
                    new Image(Kaart.class.getResource(kaardid.get(i).getPildiTee()).toExternalForm())
            );
        }

        tulemusLabel.setText("");
        kaedLabel.setText("Vali kaardid, mida HOIDA");
        kaedLabel.setMinHeight(33);

        kasKaardidJagatud.set(true);
    }

    private void vahetaKaardid() {
        // Genereerime uued kaardid nende kaaritde asemele, mida pole hoitud
        for (int i = 0; i < 5; i++) {
            if (!hoitudNupud.get(i).isSelected()) {
                Kaart uusKaart;
                do {
                    uusKaart = generaator.genereerimeKaardid(1).getFirst();
                } while (kaardid.contains(uusKaart));

                kaardid.set(i, uusKaart);
            }
            hoitudNupud.get(i).setDisable(true);
            kaartViewid.get(i).setImage(
                    new Image(Kaart.class.getResource(kaardid.get(i).getPildiTee()).toExternalForm())
            );;
        }

        // Leiame tulemuse
        käsi = Kontrollija.kontrolliPakki(kaardid);
        võit = AuhinnaValjastaja.arvutatasu(panus, käsi);
        balanss += võit;
        uuendaBalanssi();

        kaedLabel.setText("Käsi: " + käsi);
        if (võit > 0) {
            tulemusLabel.setText("Võitsid: " + String.format("%.2f", võit) + "€");
            win.play();
        } else {
            tulemusLabel.setText("Seekord ei võitnud :(");
            loss.play();
        }

        kirjutaLogisse(MänguTegevus.MÄNGU_TULEMUS);
        kasKaardidJagatud.set(false);
    }

    private void uuendaBalanssi() {
        balanssLabel.setText("Balanss: " + String.format("%.2f", balanss) + "€");
    }

    private void uuendaMituKorda(int mituKordaMängitud){
        mituKordaLabel.setText("Mitu korda mängitud: " + mituKordaMängitud);
        try (BufferedWriter kirjutaja = new BufferedWriter(new FileWriter("data/howmanytimesplayed.txt", false))) {
            kirjutaja.write(String.valueOf(mituKordaMängitud));
        } catch (IOException e) {
            System.err.println("Probleem faili 'howmanytimesplayed.txt' kirjutamisega.");
            kirjutaLogisse(MänguTegevus.ERROR, "Probleem faili 'howmanytimesplayed.txt' kirjutamisega.");
        }
    }

    // Sündmuste logimine
    private void kirjutaLogisse(MänguTegevus tegevus){
        try (BufferedWriter kirjutaja = new BufferedWriter(new FileWriter("data/log.txt", true))){
            LocalDateTime aegIlmaVorminguta = LocalDateTime.now();
            DateTimeFormatter vormindaja = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            String aeg = aegIlmaVorminguta.format(vormindaja);
            switch (tegevus) {
                case START:
                    String käima = "[" + aeg + "] PROGRAMM PANDI TÖÖLE";
                    kirjutaja.write(käima);
                    kirjutaja.newLine();

                    String balanssRida = "[" + aeg + "] ESIALGNE BALANSS: " + balanss;
                    kirjutaja.write(balanssRida);
                    kirjutaja.newLine();
                    break;
                case MÄNGU_TULEMUS:
                    String tulemus = "[" + aeg + "] MÄNGU TULEMUS - PANUS: " + panus + " | KAARDID: " + kaardid.toString() + " | KÄSI: " + käsi + " | VÕIT: " + võit + " | BALANSS: " + balanss;
                    kirjutaja.write(tulemus);
                    kirjutaja.newLine();
                    break;
            }
        } catch (IOException e) {
            System.err.println("Probleem faili 'log.txt' kirjutamisega.");
        }
    }

    // Tõrgete logimine
    private void kirjutaLogisse(MänguTegevus tegevus, String errorSisu){
        try (BufferedWriter kirjutaja = new BufferedWriter(new FileWriter("data/log.txt", true))){
            LocalDateTime aegIlmaVorminguta = LocalDateTime.now();
            DateTimeFormatter vormindaja = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            String aeg = aegIlmaVorminguta.format(vormindaja);
            if (tegevus.equals(MänguTegevus.ERROR)){
                String error = "[" + aeg + "] TEKKIS VIGA KOODIS! ERROR: " + errorSisu;
                kirjutaja.write(error);
                kirjutaja.newLine();
            }
        } catch (IOException e) {
            System.err.println("Probleem faili 'log.txt' kirjutamisega.");
        }
    }
}
