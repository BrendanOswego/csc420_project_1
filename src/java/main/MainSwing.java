package main;

import com.mpatric.mp3agic.ID3v1;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;
import net.miginfocom.swing.MigLayout;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.URL;
import java.util.*;

import static java.awt.Component.CENTER_ALIGNMENT;
import static javax.swing.SwingConstants.CENTER;
import static javax.swing.SwingConstants.VERTICAL;

//TODO- When all necessary methods are created, add Description comments for javadoc

//TODO-Add remove playlist functionality, which also means displaying available playlists to remove

//TODO- Change the previous and next images to be thicker, right now very thin looking

/**
 * Main class for application, handles most of the functionality of the app including JSON Parsing, Swing Component creation, and MP3 Data conversion from ID3 Tags
 */

public class MainSwing {
    private static final File file = new File("src/resources/json/library.json");

    private static final String play = "play_button";
    private static final String pause = "pause_button";
    private static final String previous = "previous_button";
    private static final String next = "next_button";
    private static final String shuffle = "shuffle_button";
    private static final String help = "help_button";

    private JTable songTable = new JTable();
    private JFrame jFrame;
    private JPanel libraryPanel;
    private JButton playPauseButton;
    private JButton previousButton;
    private JButton nextButton;
    private JButton shuffleButton;
    private JButton helpButton;
    private JScrollPane scrollPane;
    private final JFileChooser fileChooser = new JFileChooser();
    private final FileNameExtensionFilter fileFilter = new FileNameExtensionFilter("MP3 Files", "mp3");
    private JList<String> list;
    private DefaultListModel<String> libraryModel = new DefaultListModel<>();
    private JScrollPane playScroll;

    private String[] colNames = {"Song", "Artist", "Duration"};
    private HashMap<String, Song> songList;
    private ArrayList<String> playlistNames;
    private boolean isPlaying = false;

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new MainSwing().createDesign();
            }
        });

    }


    private void createDesign() {
        jFrame = new JFrame();
        jFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        jFrame.setSize(screenSize.width / 2, screenSize.height);
        JPanel infoPanel = new JPanel();
        JPanel mainPanel = new JPanel();
        libraryPanel = new JPanel();
        JPanel centerPanel = new JPanel();
        JPanel soundControlPanel = new JPanel();
        JPanel musicPanel = new JPanel(new FlowLayout());

        libraryPanel.setPreferredSize(new Dimension(120, jFrame.getHeight()));
        infoPanel.setPreferredSize(new Dimension(jFrame.getWidth(), 110));
        mainPanel.setPreferredSize(new Dimension(jFrame.getWidth(), jFrame.getHeight()));

        BoxLayout centerLayout = new BoxLayout(centerPanel, VERTICAL);
        BorderLayout mainLayout = new BorderLayout();
        FlowLayout playerLayout = new FlowLayout();
        BoxLayout westLayout = new BoxLayout(libraryPanel, BoxLayout.Y_AXIS);
        BoxLayout verticalLayout = new BoxLayout(infoPanel, BoxLayout.Y_AXIS);

        mainPanel.setLayout(mainLayout);
        soundControlPanel.setLayout(playerLayout);
        libraryPanel.setLayout(westLayout);
        centerPanel.setLayout(centerLayout);
        infoPanel.setLayout(verticalLayout);

        JLabel libraryHeader = new JLabel("Library");
        JLabel artistHeader = new JLabel("Artist");
        JLabel currentTime = new JLabel("0:00");
        JLabel totalTime = new JLabel("Set Time");
        JLabel title = new JLabel("Title");
        JLabel artist = new JLabel("Artist");
        JSlider musicSlider = new JSlider(JSlider.HORIZONTAL);
        previousButton = new JButton();
        playPauseButton = new JButton();
        nextButton = new JButton();
        shuffleButton = new JButton();
        helpButton = new JButton();

        createIconPNG(previousButton, previous, 20, 20);
        createIconPNG(playPauseButton, play, 20, 20);
        createIconPNG(nextButton, next, 20, 20);
        createIconPNG(shuffleButton, shuffle, 20, 20);
        createIconPNG(helpButton, help, 20, 20);

        previousButton.addActionListener(previousListener);
        playPauseButton.addActionListener(playPauseListener);
        nextButton.addActionListener(nextListener);
        shuffleButton.addActionListener(shuffleListener);


        musicSlider.setValue(0);
        musicSlider.setPreferredSize(new Dimension(250, 20));

        title.setFont(title.getFont().deriveFont(15f));
        artist.setFont(artist.getFont().deriveFont(15f));
        libraryHeader.setFont(libraryHeader.getFont().deriveFont(15f));

        title.setAlignmentX(CENTER_ALIGNMENT);
        artist.setAlignmentX(CENTER_ALIGNMENT);
        libraryHeader.setAlignmentX(CENTER_ALIGNMENT);
        artistHeader.setAlignmentX(CENTER_ALIGNMENT);
        libraryHeader.setHorizontalAlignment(CENTER);

        createIconPNG(playPauseButton, "play_button", 20, 20);

        playPauseButton.setOpaque(true);

        CustomMenuBar topMenu = new CustomMenuBar();

        int songTableWidth = (int) centerPanel.getSize().getWidth() - (int) libraryPanel.getSize().getWidth();
        int songTableHeight = (int) centerPanel.getSize().getHeight() - (int) infoPanel.getSize().getHeight();
        songTable.setPreferredSize(new Dimension(songTableWidth, songTableHeight));

        scrollPane = new JScrollPane(songTable);
        scrollPane.setPreferredSize(new Dimension((int) jFrame.getSize().getWidth(), (int) jFrame.getSize().getHeight()));

        //This has to be called before the songTable is added to the center panel
        //And anything that changes the songTable information as well i.e changing the name of a song
        initializeJson();
        initializeAddedPlaylists();

        centerPanel.add(scrollPane);
        soundControlPanel.add(previousButton);
        soundControlPanel.add(playPauseButton);
        soundControlPanel.add(nextButton);
        soundControlPanel.add(shuffleButton);

        musicPanel.add(currentTime);
        musicPanel.add(musicSlider);
        musicPanel.add(totalTime);
        musicPanel.setAlignmentX(CENTER_ALIGNMENT);

        infoPanel.add(title);
        infoPanel.add(artist);
        infoPanel.add(soundControlPanel);
        infoPanel.add(musicPanel);


        libraryPanel.add(libraryHeader);
        loadPlaylistsToPanel();

        mainPanel.add(infoPanel, BorderLayout.NORTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(libraryPanel, BorderLayout.LINE_START);

        jFrame.setJMenuBar(topMenu.showMenuBar());

        jFrame.setContentPane(mainPanel);
        jFrame.setVisible(true);
        jFrame.setFocusable(true);

        fillEmptyRows();
    }

    private void initializeJson() {

        ArrayList<String> jsonIdList = new ArrayList<>();
        songList = new HashMap<>();

        DefaultTableModel dataModel = new DefaultTableModel(colNames, 0);

        JSONParser parser = new JSONParser();
        Song tempSong;
        try {
            Object obj = parser.parse(new FileReader(file));
            JSONObject jsonObject = (JSONObject) obj;
            JSONObject library = (JSONObject) jsonObject.get("library");
            JSONArray playlistArr = (JSONArray) library.get("playlist");

            for (int i = 0; i < playlistArr.size(); i++) {
                JSONObject playElement = (JSONObject) playlistArr.get(i);
                String playlistName = (String) playElement.get("name");
                if (playlistName.equalsIgnoreCase("default")) {
                    JSONArray songArr = (JSONArray) playElement.get("song");
                    for (int j = 0; j < songArr.size(); j++) {
                        JSONObject songElement = (JSONObject) songArr.get(j);
                        if (songElement != null) {
                            String title = (String) songElement.get("title");
                            String id = (String) songElement.get("id");
                            jsonIdList.add(id);
                            String artist = (String) songElement.get("artist");
                            String duration = (String) songElement.get("duration");
                            tempSong = new Song(id, title, artist, null, duration);
                            songList.put(id, tempSong);
                            Object[] rowObj = {tempSong.getTitle(), tempSong.getArtist(), tempSong.getDuration()};
                            dataModel.addRow(rowObj);
                            scrollPane.getViewport().revalidate();
                        }

                    }
                }

            }

        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

        songTable.setFocusable(false);
        songTable.setRowSelectionAllowed(true);
        songTable.setAutoCreateRowSorter(true);
        songTable.setFillsViewportHeight(true);
        songTable.setModel(dataModel);
        songTable.setDefaultRenderer(Object.class, new CustomCellRender());
        songTable.setComponentPopupMenu(showPopupMenu());
    }


    private void loadPlaylistToTable(String name) {

        DefaultTableModel dataModel = new DefaultTableModel(colNames, 0);

        JSONParser parser = new JSONParser();

        try {
            Object obj = parser.parse(new FileReader(file));
            JSONObject jsonObject = (JSONObject) obj;
            JSONObject library = (JSONObject) jsonObject.get("library");
            JSONArray playlistArr = (JSONArray) library.get("playlist");
            for (int i = 0; i < playlistArr.size(); i++) {
                JSONObject playElement = (JSONObject) playlistArr.get(i);
                String playName = (String) playElement.get("name");
                if (playName != null)
                    if (name.equals(playName)) {
                        JSONArray songArr = (JSONArray) playElement.get("song");
                        for (int j = 0; j < songArr.size(); j++) {
                            JSONObject songElement = (JSONObject) songArr.get(j);
                            if (songElement != null) {
                                String id = (String) songElement.get("id");
                                System.out.println(id);
                                if (songList.containsKey(id)) {
                                    Object[] row = {songList.get(id).getTitle(), songList.get(id).getArtist(), songList.get(id).getDuration()};
                                    dataModel.addRow(row);
                                    scrollPane.getViewport().revalidate();
                                }

                            }
                            fillEmptyRows();
                        }
                    }
            }

        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

        songTable.setFocusable(false);
        songTable.setRowSelectionAllowed(true);
        songTable.setAutoCreateRowSorter(true);
        songTable.setFillsViewportHeight(true);
        songTable.setModel(dataModel);
        songTable.setDefaultRenderer(Object.class, new CustomCellRender());
        songTable.setComponentPopupMenu(showPopupMenu());
    }

    private void initializeAddedPlaylists() {
        playlistNames = new ArrayList<>();

        JSONParser parser = new JSONParser();
        System.out.println("Initialized loading playlist names");
        try {
            Object obj = parser.parse(new FileReader(file));
            JSONObject jsonObject = (JSONObject) obj;
            JSONObject library = (JSONObject) jsonObject.get("library");
            JSONArray playlistArr = (JSONArray) library.get("playlist");
            for (int i = 0; i < playlistArr.size(); i++) {
                JSONObject playElement = (JSONObject) playlistArr.get(i);
                String playName = (String) playElement.get("name");
                if (playName != null) {
                    playlistNames.add(playName);
                }

            }
            System.out.println("Playlists added: " + playlistNames.toString());

        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }


    }

    void addSelectedSongToPlaylist(Song song, String playlistName) {

    }


    private void createPlaylist(String name) {
        JSONParser parser = new JSONParser();
        Object obj = null;
        try {
            obj = parser.parse(new FileReader(file));
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

        JSONObject jsonObject = (JSONObject) obj;
        JSONObject library = (JSONObject) jsonObject.get("library");
        JSONArray playArr = (JSONArray) library.get("playlist");

        JSONObject newEntry = new JSONObject();
        newEntry.put("song", new JSONArray());
        newEntry.put("name", name);

        playArr.add(newEntry);

        try {
            System.out.println("Writing to JSON");
            FileWriter writer = new FileWriter(file);
            writer.write(jsonObject.toJSONString());
            writer.flush();
            writer.close();
            try {
                Thread.sleep(40);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Wrote to JSON");
            System.out.println(jsonObject.toString());

        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    private void loadPlaylistsToPanel() {
        JSONParser parser = new JSONParser();
        System.out.println("Initialized loading playlist names");
        if (list == null) {
            list = new JList<>(libraryModel);
            playScroll = new JScrollPane(list);
            list.setFont(list.getFont().deriveFont(15f));
            list.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
            list.setLayoutOrientation(JList.VERTICAL);
            list.setVisibleRowCount(-1);
        } else {
            list.removeAll();
            libraryModel.removeAllElements();
        }
        try {
            Object obj = parser.parse(new FileReader(file));
            JSONObject jsonObject = (JSONObject) obj;
            JSONObject library = (JSONObject) jsonObject.get("library");
            JSONArray playlistArr = (JSONArray) library.get("playlist");
            for (int i = 0; i < playlistArr.size(); i++) {
                JSONObject playElement = (JSONObject) playlistArr.get(i);
                String playName = (String) playElement.get("name");
                JLabel playLabel = new JLabel();
                if (playName.equals("default")) {
                    playLabel.setText("All Songs");
                } else {
                    playLabel.setText(playName);
                }
                libraryModel.addElement(playLabel.getText());
                list.addMouseListener(mouseListener);
                if ((int) playLabel.getSize().getWidth() > (int) libraryPanel.getSize().getWidth()) {
                    list.setFont(list.getFont().deriveFont(10f));
                }
            }

            libraryPanel.add(playScroll);

        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

    }

    private MouseListener mouseListener = new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
            JList theList = (JList) e.getSource();
            if (e.getClickCount() == 2) {
                int index = theList.locationToIndex(e.getPoint());
                if (index >= 0) {
                    String name = (String) theList.getModel().getElementAt(index);
                    if (name.equals("All Songs")) {
                        loadPlaylistToTable("default");
                    } else {
                        loadPlaylistToTable(name);
                    }
                    fillEmptyRows();
                }
            }
        }
    };

    private ActionListener previousListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            //TODO-Add functionality to play previous song...maybe have separate class for MP3 data?
        }
    };

    private ActionListener nextListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            //TODO-Add functionality to play next song...maybe have separate class for MP3 data?
        }
    };
    private ActionListener shuffleListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            //TODO-Add shuffle functionality
        }
    };

    private ActionListener playPauseListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (!isPlaying) {
                createIconPNG(playPauseButton, pause, 20, 20);
                isPlaying = true;
            } else {
                createIconPNG(playPauseButton, play, 20, 20);
                isPlaying = false;
            }
        }
    };


    private void fillEmptyRows() {
        int rows = songTable.getRowCount();
        int rowHeight = songTable.getRowHeight();
        int tableHeight = songTable.getTableHeader().getHeight() + (rows * rowHeight);
        while (tableHeight < scrollPane.getViewport().getHeight()) {
            ((DefaultTableModel) songTable.getModel()).addRow(new Object[]{null, null, null});
            tableHeight += rowHeight;
        }
    }

    private void showFileChooser() {
        fileChooser.setFileFilter(fileFilter);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int returnVal = fileChooser.showOpenDialog(jFrame);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            Mp3File file = null;
            try {
                file = new Mp3File(fileChooser.getSelectedFile());
            } catch (IOException | UnsupportedTagException | InvalidDataException e) {
                e.printStackTrace();
            }
            if (file != null && file.hasId3v1Tag()) {
                ID3v1 id3v1Tag = file.getId3v1Tag();
                System.out.println(id3v1Tag.getArtist());
                System.out.println(id3v1Tag.getTitle());
                //TODO-Get necessary info and call addSongToLibrary(Song s) method
            }
        } else {
            System.out.println("File Chooser Cancelled by User");
        }
    }


    private JPopupMenu showPopupMenu() {
        JPopupMenu optionMenu = new JPopupMenu();
        JMenuItem itemPlay = new JMenuItem("Play");
        optionMenu.add(itemPlay);
        JMenuItem itemAdd = new JMenuItem("Add To Playlist");
        optionMenu.add(itemAdd);
        JMenu subMenu = new JMenu();
        for (int i = 0; i < 4; i++) {
            JMenuItem item = new JMenuItem(String.valueOf(i));
            subMenu.add(item);
        }
        JMenuItem itemInfo = new JMenuItem("Get Info");
        optionMenu.add(itemInfo);
        JMenuItem itemDelete = new JMenuItem("Delete");
        optionMenu.add(itemDelete);

        return optionMenu;
    }

    private ImageIcon findImagePath(String path) {
        URL imgUrl = MainSwing.class.getResource(path);
        if (imgUrl != null) {
            return new ImageIcon(imgUrl);
        } else {
            System.err.println("No content found");
            return null;
        }
    }

    private void createIcon(JLabel label, String name, int width, int height) {
        ImageIcon icon = findImagePath("/images/" + name + ".gif");
        if (icon != null) {
            Image image = icon.getImage();
            Image scaledImage = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            icon = new ImageIcon(scaledImage);
            label.setIcon(icon);
        } else {
            System.err.println("GIF " + name + " was not found");
        }

    }

    private void createIconPNG(JButton label, String name, int width, int height) {
        //Uses above method and sets the icon to the local JLabel
        ImageIcon icon = findImagePath("/images/" + name + ".png");
        if (icon != null) {
            Image image = icon.getImage();
            Image scaledImage = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            icon = new ImageIcon(scaledImage);
            label.setIcon(icon);
        } else {
            System.err.println("PNG " + name + "  was  not found");
        }

    }


    public ArrayList<String> getPlaylistNames() {
        return playlistNames;
    }

    public void setPlaylistNames(ArrayList<String> playlistNames) {
        this.playlistNames = playlistNames;
    }

    public int getPlaylistSize() {
        return playlistNames.size();
    }

    /**
     * Inner class that creates the JMenu for the main JFrame to use that hosts the components for the top menu
     */
    private class CustomMenuBar extends JMenuBar {


        JMenuBar showMenuBar() {

            JMenuBar menuBar = new JMenuBar();
            menuBar.setBackground(Color.black);

            JMenu menu = new JMenu("File");
            menu.setBackground(Color.black);
            menuBar.add(menu);
            JMenuItem item = new JMenuItem("Add Song");
            item.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    showFileChooser();
                }
            });
            menu.add(item);

            menu = new JMenu("Edit");
            menu.setBackground(Color.black);
            menuBar.add(menu);

            item = new JMenuItem("Undo");
            menu.add(item);

            item = new JMenuItem("Redo");
            menu.add(item);


            menu = new JMenu("Playlist");
            menu.setBackground(Color.black);
            menuBar.add(menu);

            item = new JMenuItem("Create New Playlist");
            item.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {

                    JDialog dialog = new JDialog();
                    dialog.setLayout(new MigLayout("align 50% 50%"));
                    int width = 250;
                    int height = 150;
                    int xPos = ((int) getSize().getWidth() - width) / 2;
                    int yPos = ((int) getSize().getHeight() - height) / 2;
                    dialog.setBounds(xPos, yPos, width, height);
                    JLabel title = new JLabel("Enter new Playlist name");
                    JButton submit = new JButton("Submit");
                    JButton cancel = new JButton("Cancel");
                    JTextField field = new JTextField(10);

                    dialog.add(title, "span");
                    dialog.add(field, "span");
                    dialog.add(submit);
                    dialog.add(cancel);

                    dialog.pack();
                    dialog.setVisible(true);
                    dialog.setLocationRelativeTo(jFrame);


                    cancel.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            //TODO- Close Dialog and clear text
                        }
                    });
                    submit.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            if (!field.getText().isEmpty()) {
                                System.out.println("Performing Action");
                                createPlaylist(field.getText());
                                playlistNames.add(field.getText());
                                initializeAddedPlaylists();
                                loadPlaylistsToPanel();
                                //TODO-Create method that adds the newly added playlist to the menu a runtime
                                dialog.setVisible(false);
                                menuBar.revalidate();
                            }
                        }
                    });

                }
            });
            menu.add(item);
            menu.addSeparator();
            JMenu subMenu = new JMenu("Open Playlist");
            menu.add(subMenu);
            JMenuItem playlistItem;
            for (int i = 0; i < playlistNames.size(); i++) {
                playlistItem = new JMenuItem(playlistNames.get(i));
                if (playlistItem.getText().equals("default")) {
                    playlistItem.setText("Library");
                } else {

                }
                int finalI = i;
                playlistItem.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (playlistNames.get(finalI).equals("Library")) {
                            loadPlaylistToTable(playlistNames.get(0));
                        } else {
                            loadPlaylistToTable(playlistNames.get(finalI));
                        }
                    }
                });
                subMenu.add(playlistItem);
            }

            subMenu = new JMenu("Add to Playlist");
            menu.add(subMenu);
            for (int i = 0; i < playlistNames.size(); i++) {
                item = new JMenuItem(playlistNames.get(i));
                subMenu.add(item);
            }
            menuBar.add(Box.createHorizontalGlue());
            createIconPNG(helpButton, help, 20, 20);
            helpButton.setBackground(Color.black);
            menuBar.add(helpButton);
            return menuBar;


        }


    }
}
