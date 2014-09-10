/*
 * JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 * Alex Barke
 * 860883440
 *
 * Willy Yong
 * 861032168
 */
import java.awt.*;
import java.applet.*;
import java.awt.event.*;
import javax.swing.event.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.*;
import java.io.*;
import java.util.*; // used for reading in ints

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */

public class EmbeddedSQL {

static String currentUser = "";
static boolean superUser = false;
static Vector<Vector> data = new Vector<Vector>();
static Vector<String> rowData = new Vector<String>();
static Vector<String> colName = new Vector<String>();
static EmbeddedSQL esql = null;
static ResultSet rs;

// reference to physical database connection.
private static Connection _connection = null;
//private static Statement stmt;

// handling the keyboard inputs through a BufferedReader
// This variable can be global for convenience.
static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

   /**
    * Creates a new instance of EmbeddedSQL
    *
    * @param hostname the MySQL or PostgreSQL server hostname
    * @param database the name of the database
    * @param username the user name used to login to the database
    * @param password the user login password
    * @throws java.sql.SQLException when failed to make a connection.
    */
	public EmbeddedSQL (String dbname, String dbport, String user, String passwd) throws SQLException {

		System.out.print("Connecting to database...");
		try{
			// constructs the connection URL
			String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
			System.out.println ("Connection URL: " + url + "\n");

			// obtain a physical connection
			this._connection = DriverManager.getConnection(url, user, passwd);
			System.out.println("Done");
		}catch (Exception e){
			System.err.println("Error - Unable to Connect to Database: " + e.getMessage() );
			System.out.println("Make sure you started postgres on this machine");
			System.exit(-1);
		}//end catch
	}//end EmbeddedSQL

   /**
    * Method to execute an update SQL statement.  Update SQL instructions
    * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
    *
    * @param sql the input SQL string
    * @throws java.sql.SQLException when update failed
    */
	public void executeUpdate (String sql) throws SQLException {
		// creates a statement object
		Statement stmt = this._connection.createStatement ();
		// issues the update instruction
		stmt.executeUpdate (sql);
		// close the instruction
		stmt.close ();
	}//end executeUpdate

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and outputs the results to
    * standard out.
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
	public int executeQuery (String query) throws SQLException {
		// creates a statement object
		Statement stmt = EmbeddedSQL._connection.createStatement ();

		// issues the query instruction
		//ResultSet rs = stmt.executeQuery (query);
		rs = stmt.executeQuery (query);
		/*
		** obtains the metadata object for the returned result set.  The metadata
		** contains row and column info.
		*/
		ResultSetMetaData rsmd = rs.getMetaData ();
		int numCol = rsmd.getColumnCount ();
		int rowCount = 0;
		colName.clear();
		//rowData.clear();
		data.clear();
		Vector vec = new Vector(numCol);
		// iterates through the result set and output them to standard out.
		boolean outputHeader = true;
		while (rs.next()){
			if(outputHeader){
				for(int i = 1; i <= numCol; i++){
					colName.add(rsmd.getColumnName(i));
					//System.out.print(rsmd.getColumnName(i) + "\t");
				}
				//System.out.println();
				outputHeader = false;
			}
			for (int i=1; i<=numCol; ++i)
			{
				//System.out.print (rs.getString (i) + "\t");
				vec.add(rs.getString (i));
				rowData.add(rs.getString(i));
			}
				//System.out.print (rs.getString (i) + "\t");
			//System.out.println ();
			data.add(vec);
			++rowCount;
		}//end while
		stmt.close ();
		return rowCount;
	}//end executeQuery

	public static JTable createTable (String query) throws SQLException {
		Statement stmt = EmbeddedSQL._connection.createStatement();
		
		// issues the query instruction
		//ResultSet rs = stmt.executeQuery (query);
		rs = stmt.executeQuery (query);
		ResultSetMetaData rsmd = rs.getMetaData ();
		int numCol = rsmd.getColumnCount ();
		int rowCount = 0;
		Vector vectorOfVector = new Vector(numCol);
		Vector<String> header = new Vector<String>();
		
		// iterates through the result set and output them to standard out.
		boolean outputHeader = true;
		while (rs.next()){
		
		Vector vec = new Vector(numCol);   // create a vector for each row
		
		if(outputHeader){
			for(int i = 1; i <= numCol; i++){
				header.add(rsmd.getColumnName(i));
			}
			outputHeader = false;
		}
			for (int i=1; i<=numCol; ++i)
				vec.add(rs.getString(i));
		vectorOfVector.add(vec);
		}//end while
		//DefaultTableModel model = new DefaultTableModel(vectorOfVector, header);
		//table.setModel(model);
		JTable table = new JTable(vectorOfVector,header);
		stmt.close ();
		
		return table;
	}
	
	public static String getSQL (String query) throws SQLException {
		Statement stmt = EmbeddedSQL._connection.createStatement();
		
		// issues the query instruction
		//ResultSet rs = stmt.executeQuery (query);
		rs = stmt.executeQuery (query);
		ResultSetMetaData rsmd = rs.getMetaData ();
		int numCol = rsmd.getColumnCount ();
		String sqlData="";

		// iterates through the result set and output them to standard out.
		boolean outputHeader = true;
		while (rs.next()){

		if(outputHeader){
			for(int i = 1; i <= numCol; i++){
				sqlData+=rsmd.getColumnName(i)+"\t";
				//header.add(rsmd.getColumnName(i));
			}
			sqlData+="\n";
			outputHeader = false;
		}
			for (int i=1; i<=numCol; ++i)
				sqlData+=rs.getString(i)+"\t";
			sqlData+="\n";
		}//end while
		stmt.close ();
		return sqlData;
	}

   /**
    * Method to close the physical connection if it is open.
    */
	public void cleanup(){
		try{
			if (this._connection != null){
				this._connection.close ();
			}//end if
		}catch (SQLException e){
			// ignored.
		}//end try
	}//end cleanup

   /**
    * The main execution method
    *
    * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
    */
	public static void main (String[] args) {
		String currUser="";
		boolean keepOn=false;
		if (args.length != 4) 
		{
			System.err.println ("Usage: "+"java [-classpath <classpath>] " + EmbeddedSQL.class.getName ()+" <dbname> <port> <user> <passwd>");
			return;
		}
		//Greeting();
		//EmbeddedSQL esql = null;
		//EmbeddedSQL static esql = null;
		try{
			// use postgres JDBC driver.
			Class.forName ("org.postgresql.Driver").newInstance ();
			// instantiate the EmbeddedSQL object and creates a physical
			// connection.
			String dbname = args[0];
			String dbport = args[1];
			String user = args[2];
			String passwd = args[3];
			esql = new EmbeddedSQL (dbname, dbport, user, passwd);

			javax.swing.SwingUtilities.invokeLater(new Runnable() {
				public void run() {
				//======================================================================//
									  // Login Frame //
				//======================================================================//
					
					final JFrame frLogin = new JFrame("MovieNet");
					frLogin.setLayout(null);
					frLogin.setBounds(0,0,700,645);
					frLogin.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
					
					//lblUsuario
					JLabel lblUsuario = new JLabel("User");
					lblUsuario.setBounds(100,200,100,50);
					lblUsuario.setFont(new Font("serif",Font.BOLD,18));
					lblUsuario.setVerticalAlignment(SwingConstants.CENTER);
					frLogin.getContentPane().add(lblUsuario);

					//txtUser
					final JTextField txtUser = new JTextField();
					txtUser.setBounds(200,212,150,25);
					txtUser.setFont(new Font("sansserif",Font.ITALIC,14));
					frLogin.getContentPane().add(txtUser);

					//lblContrasena
					JLabel lblContrasena = new JLabel("Password");
					lblContrasena.setBounds(80,280,100,50);
					lblContrasena.setFont(new Font("serif",Font.BOLD,18));
					lblContrasena.setVerticalAlignment(SwingConstants.CENTER);
					frLogin.getContentPane().add(lblContrasena);

					//txtPassword
					final JTextField txtPassword = new JPasswordField();
					txtPassword.setBounds(200,292,150,25);
					txtPassword.setFont(new Font("sansserif",Font.BOLD,16));
					//txtPassword.addActionListener(this);
					frLogin.getContentPane().add(txtPassword);

					//btnEnter
					JButton btnEnter = new JButton("login");
					btnEnter.setBounds(450,212,100,25);
					//btnEnter.addActionListener(this);
					//btnEnter.setToolTipText("Login to Main Menu");
					btnEnter.setMnemonic('E');
					frLogin.getContentPane().add(btnEnter);

					//btnAbout
					//JButton btnAbout = new JButton("About");
					//btnAbout.setBounds(312,410,100,25);
					//btnAbout.addActionListener(this);
					//btnAbout.setToolTipText("Information about Team");
					//frLogin.getContentPane().add(btnAbout);

					//btnRegister
					JButton btnRegister = new JButton("Click To Register");
					btnRegister.setBounds(260,440,200,25);
					frLogin.getContentPane().add(btnRegister);

					//btnExit
					JButton btnExit = new JButton("Exit");
					btnExit.setBounds(450,252,100,25);
					//btnErase.addActionListener(this);
					btnExit.setToolTipText("Exits Program");
					frLogin.getContentPane().add(btnExit);

					//lblIcono
					//JLabel lblIcono = new JLabel(new ImageIcon("bin/Icono.gif"));
					//lblIcono.setBounds(250,50,232,46);
					//frLogin.getContentPane().add(lblIcono);

					//lblFondoU
					JLabel lblFondoU = new JLabel(new ImageIcon("etc/wallpaper/02.jpg"));
					lblFondoU.setBounds(0,0,700,645);
					frLogin.getContentPane().add(lblFondoU);	

					frLogin.setVisible(true);
					
					//======================================================================//
										// Register Menu //
					//======================================================================//
					
					//Main Frame
					final JFrame frRegister = new JFrame("Register");
					frRegister.setLayout(null);
					frRegister.setBounds(0,0,700,645);
					frRegister.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
					
					//btnRegOk
					JButton btnRegOk = new JButton("Register");
					btnRegOk.setBounds(500,40,100,25);
					btnRegOk.setCursor(new Cursor(Cursor.HAND_CURSOR));
					frRegister.getContentPane().add(btnRegOk);
					
					//btnReturn
					JButton btnReturn = new JButton("Return");
					btnReturn.setBounds(500,75,100,25);
					btnReturn.setCursor(new Cursor(Cursor.HAND_CURSOR));
					frRegister.getContentPane().add(btnReturn);
					
					// username label
					JLabel username_label = new JLabel("Username*");
					username_label.setBounds(50,40,200,30);
					username_label.setFont(new Font("serif",Font.BOLD,18));
					username_label.setVerticalAlignment(SwingConstants.CENTER);
					frRegister.getContentPane().add(username_label);
				
					//username text field
					final JTextField txtUsername = new JTextField();
					txtUsername.setBounds(250,45,200,25);
					txtUsername.setFont(new Font("sansserif",Font.ITALIC,14));
					frRegister.getContentPane().add(txtUsername);
					
					// password label
					JLabel password_label = new JLabel("Password*");
					password_label.setBounds(50,70,200,30);
					password_label.setFont(new Font("serif",Font.BOLD,18));
					password_label.setVerticalAlignment(SwingConstants.CENTER);
					frRegister.getContentPane().add(password_label);
				
					//password text field
					final JTextField RegtxtPassword = new JPasswordField();
					RegtxtPassword.setBounds(250,75,200,25);
					RegtxtPassword.setFont(new Font("sansserif",Font.ITALIC,14));
					frRegister.getContentPane().add(RegtxtPassword);
					
					// First name label
					JLabel reg_fname_label = new JLabel("First name*");
					reg_fname_label.setBounds(50,100,200,30);
					reg_fname_label.setFont(new Font("serif",Font.BOLD,18));
					reg_fname_label.setVerticalAlignment(SwingConstants.CENTER);
					frRegister.getContentPane().add(reg_fname_label);
				
					// First name text field
					final JTextField reg_fname_txt = new JTextField();
					reg_fname_txt.setBounds(250,105,200,25);
					reg_fname_txt.setFont(new Font("sansserif",Font.ITALIC,14));
					frRegister.getContentPane().add(reg_fname_txt);
					
					// Middle Initial label
					JLabel reg_mname_label = new JLabel("Middle name");
					reg_mname_label.setBounds(50,130,150,30);
					reg_mname_label.setFont(new Font("serif",Font.BOLD,18));
					reg_mname_label.setVerticalAlignment(SwingConstants.CENTER);
					frRegister.getContentPane().add(reg_mname_label);
					
					// Middle initial text field
					final JTextField reg_mname_txt = new JTextField();
					reg_mname_txt.setBounds(250,135,200,25);
					reg_mname_txt.setFont(new Font("sansserif",Font.ITALIC,14));
					frRegister.getContentPane().add(reg_mname_txt);
					
					// Last name label
					JLabel reg_lname_label = new JLabel("Last name*");
					reg_lname_label.setBounds(50,160,200,30);
					reg_lname_label.setFont(new Font("serif",Font.BOLD,18));
					reg_lname_label.setVerticalAlignment(SwingConstants.CENTER);
					frRegister.getContentPane().add(reg_lname_label);
					
					// Last name text field
					final JTextField reg_lname_txt = new JTextField();
					reg_lname_txt.setBounds(250,165,200,25);
					reg_lname_txt.setFont(new Font("sansserif",Font.ITALIC,14));
					frRegister.getContentPane().add(reg_lname_txt);
					
					// email label
					JLabel reg_email_label = new JLabel("Email*");
					reg_email_label.setBounds(50,190,200,30);
					reg_email_label.setFont(new Font("serif",Font.BOLD,18));
					reg_email_label.setVerticalAlignment(SwingConstants.CENTER);
					frRegister.getContentPane().add(reg_email_label);
					
					// email text field
					final JTextField reg_email_txt = new JTextField();
					reg_email_txt.setBounds(250,195,200,25);
					reg_email_txt.setFont(new Font("sansserif",Font.ITALIC,14));
					frRegister.getContentPane().add(reg_email_txt);
					
					// street1 label
					JLabel reg_street1_label = new JLabel("Street1");
					reg_street1_label.setBounds(50,220,200,30);
					reg_street1_label.setFont(new Font("serif",Font.BOLD,18));
					reg_street1_label.setVerticalAlignment(SwingConstants.CENTER);
					frRegister.getContentPane().add(reg_street1_label);
					
					// street1 text field
					final JTextField reg_street1_txt = new JTextField();
					reg_street1_txt.setBounds(250,225,200,25);
					reg_street1_txt.setFont(new Font("sansserif",Font.ITALIC,14));
					frRegister.getContentPane().add(reg_street1_txt);
					
					// street2 label
					JLabel reg_street2_label = new JLabel("Street2");
					reg_street2_label.setBounds(50,250,200,30);
					reg_street2_label.setFont(new Font("serif",Font.BOLD,18));
					reg_street2_label.setVerticalAlignment(SwingConstants.CENTER);
					frRegister.getContentPane().add(reg_street2_label);
					
					// street2 text field
					final JTextField reg_street2_txt = new JTextField();
					reg_street2_txt.setBounds(250,255,200,25);
					reg_street2_txt.setFont(new Font("sansserif",Font.ITALIC,14));
					frRegister.getContentPane().add(reg_street2_txt);
					
					// state label
					JLabel reg_state_label = new JLabel("State");
					reg_state_label.setBounds(50,280,200,30);
					reg_state_label.setFont(new Font("serif",Font.BOLD,18));
					reg_state_label.setVerticalAlignment(SwingConstants.CENTER);
					frRegister.getContentPane().add(reg_state_label);
					
					//state text field
					final JTextField reg_state_txt = new JTextField();
					reg_state_txt.setBounds(250,285,200,25);
					reg_state_txt.setFont(new Font("sansserif",Font.ITALIC,14));
					frRegister.getContentPane().add(reg_state_txt);
					
					// country label
					JLabel reg_country_label = new JLabel("Country");
					reg_country_label.setBounds(50,310,200,30);
					reg_country_label.setFont(new Font("serif",Font.BOLD,18));
					reg_country_label.setVerticalAlignment(SwingConstants.CENTER);
					frRegister.getContentPane().add(reg_country_label);
					
					// country text field
					final JTextField reg_country_txt = new JTextField();
					reg_country_txt.setBounds(250,315,200,25);
					reg_country_txt.setFont(new Font("sansserif",Font.ITALIC,14));
					frRegister.getContentPane().add(reg_country_txt);
					
					// zipcode label
					JLabel reg_zip_label = new JLabel("Zip Code");
					reg_zip_label.setBounds(50,340,200,30);
					reg_zip_label.setFont(new Font("serif",Font.BOLD,18));
					reg_zip_label.setVerticalAlignment(SwingConstants.CENTER);
					frRegister.getContentPane().add(reg_zip_label);
					
					//zipcode text field
					final JTextField reg_zip_txt = new JTextField();
					reg_zip_txt.setBounds(250,345,200,25);
					reg_zip_txt.setFont(new Font("sansserif",Font.ITALIC,14));
					frRegister.getContentPane().add(reg_zip_txt);
					
					// error label
					final JLabel reg_error_label = new JLabel("");
					reg_error_label.setBounds(50,370,400,300);
					reg_error_label.setFont(new Font("serif",Font.BOLD,14));
					reg_error_label.setVerticalAlignment(SwingConstants.CENTER);
					frRegister.getContentPane().add(reg_error_label);	
					
					//======================================================================//
										// Main Menu //
					//======================================================================//
					
					//Main Frame
					final JFrame frUserMenu = new JFrame("MovieNet");
					frUserMenu.setLayout(null);
					frUserMenu.setBounds(0,0,700,645);
					frUserMenu.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
					
					//btnWatchMovie
					JButton btnWatchMovie = new JButton("Watch Movie");
					btnWatchMovie.setBounds(265,200,160,25);
					btnWatchMovie.setCursor(new Cursor(Cursor.HAND_CURSOR));
					frUserMenu.getContentPane().add(btnWatchMovie);
					
					//btnChargeAccount
					JButton btnChargeAccount = new JButton("Charge Account");
					btnChargeAccount.setBounds(265,260,160,25);
					btnChargeAccount.setCursor(new Cursor(Cursor.HAND_CURSOR));
					frUserMenu.getContentPane().add(btnChargeAccount);
					
					//btnRateMovie
					JButton btnRateMovie = new JButton("Rate Movie");
					btnRateMovie.setBounds(265,320,160,25);
					btnRateMovie.setCursor(new Cursor(Cursor.HAND_CURSOR));
					frUserMenu.getContentPane().add(btnRateMovie);
					
					//btnAddFavorites
					JButton btnAddFavorites = new JButton("Add to Favorites");
					btnAddFavorites.setBounds(265,380,160,25);
					btnAddFavorites.setCursor(new Cursor(Cursor.HAND_CURSOR));
					frUserMenu.getContentPane().add(btnAddFavorites);
					
					//btnSeeWall
					JButton btnSeeWall = new JButton("See Wall");
					btnSeeWall.setBounds(265,440,160,25);
					btnSeeWall.setCursor(new Cursor(Cursor.HAND_CURSOR));
					frUserMenu.getContentPane().add(btnSeeWall);
					
					//lblTitle
					JLabel lblTitle = new JLabel("MOVIENET MAIN MENU");
					lblTitle.setBounds(218,100,300,50);
					lblTitle.setFont(new Font("monospaced",Font.BOLD,22));
					frUserMenu.getContentPane().add(lblTitle);
					
					//btnLogout
					JButton btnLogout = new JButton("Logout");
					btnLogout.setBounds(520,540,80,25);
					btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
					frUserMenu.getContentPane().add(btnLogout);
					
					//lblFondoP
					JLabel lblFondoP = new JLabel(new ImageIcon("etc/wallpaper/01.jpg"));
					lblFondoP.setBounds(0,0,700,645);
					frUserMenu.getContentPane().add(lblFondoP);
					
					//======================================================================//
										// Watch Movie //
					//======================================================================//
					
					//Main Frame
					final JFrame frWatchMovie = new JFrame("Watch Movie");
					frWatchMovie.setLayout(null);
					frWatchMovie.setBounds(0,0,700,645);
					//frWatchMovie.setBackground(Color.green);
					frWatchMovie.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
					
					// Back Button
					JButton btnWatchReturn = new JButton("<- Back");
					btnWatchReturn.setBounds(100,35,200,25);
					btnWatchReturn.setCursor(new Cursor(Cursor.HAND_CURSOR));
					frWatchMovie.getContentPane().add(btnWatchReturn);
					
					// Choose movie label
					JLabel lblWatch = new JLabel("Choose a Movie to Watch (by video id)");
					lblWatch.setBounds(50,400,300,25);
					lblWatch.setFont(new Font("monospaced",Font.ITALIC,14));
					frWatchMovie.getContentPane().add(lblWatch);

					// Label Balance
					JLabel lblBal1 = new JLabel("Balance: ");
					lblBal1.setBounds(350,35,150,25);
					lblBal1.setFont(new Font("monospaced",Font.ITALIC,14));
					frWatchMovie.getContentPane().add(lblBal1);

					// Label Balance
					final JLabel lblBal2 = new JLabel("$ 0.00");
					lblBal2.setBounds(450,35,150,25);
					lblBal2.setFont(new Font("monospaced",Font.BOLD + Font.ITALIC,14));
					frWatchMovie.getContentPane().add(lblBal2);
					
					// Choose movie text field
					final JTextField txtwatchMovie = new JTextField();
					txtwatchMovie.setBounds(350,400,100,25);
					txtwatchMovie.setFont(new Font("sansserif",Font.ITALIC,14));
					frWatchMovie.getContentPane().add(txtwatchMovie);
					
					// Place Watch DVD Button
					JButton btnWatchDVD = new JButton("Watch on DVD");
					btnWatchDVD.setBounds(200,500,200,25);
					btnWatchDVD.setCursor(new Cursor(Cursor.HAND_CURSOR));
					frWatchMovie.getContentPane().add(btnWatchDVD);
					
					// Place Watch Online Button
					JButton btnWatchOnline = new JButton("Watch Online");
					btnWatchOnline.setBounds(200,550,200,25);
					btnWatchOnline.setCursor(new Cursor(Cursor.HAND_CURSOR));
					frWatchMovie.getContentPane().add(btnWatchOnline);
				/*
					//List Movie Button
					JButton btnListWatchMovie = new JButton("List Movies");
					btnListWatchMovie.setBounds(300,35,300,25);
					btnListWatchMovie.setCursor(new Cursor(Cursor.HAND_CURSOR));
					frWatchMovie.getContentPane().add(btnListWatchMovie);
					
					JRadioButton jrbDvd = new JRadioButton("DVD");
					//jrbNumbers.setMnemonic(KeyEvent.VK_N);
					
					jrbDvd.setActionCommand("DVD");
					jrbDvd.setSelected(true);
					
					JRadioButton jrbOnline = new JRadioButton("Online");
					jrbOnline.setActionCommand("Online");
	
					// Group the radio buttons.
					ButtonGroup group = new ButtonGroup();
					group.add(jrbDvd);
					group.add(jrbOnline);
					
					// Register an action listener for the radio buttons.
					myListener = new RadioListener();
					jrbDvd.addActionListener(myListener);
					jrbOnline.addActionListener(myListener);
					jrbSymbols.addActionListener(myListener);*/
					/*
					// Rate Movie txtSC2
					final JTextArea watchMovietxtSC2 = new JTextArea();
					watchMovietxtSC2.setEditable(false);
					frWatchMovie.getContentPane().add(watchMovietxtSC2);
					
					// Rate Movie scpScrollC2
					JScrollPane watchMoviecpScrollC2 = new JScrollPane(watchMovietxtSC2);
					watchMoviecpScrollC2.setBounds(50,100,570,300);
					watchMoviecpScrollC2.setForeground(Color.cyan);
					frWatchMovie.getContentPane().add(watchMoviecpScrollC2);*/
					
					/*
					
					JTable table = new JTable(data, columnNames);
					table.setFillsViewportHeight(true);
					JScrollPane watchMoviecpScrollC2 = new JScrollPane(table);
					watchMoviecpScrollC2.setBounds(50,100,570,300);
					watchMoviecpScrollC2.setForeground(Color.cyan);
					frWatchMovie.getContentPane().add(watchMoviecpScrollC2);*/
					
					
					
					//======================================================================//
					                    // Add Funds //
					//======================================================================//			
					
					// Main Frame
					final JFrame AFfrChargeAccount = new JFrame("Add Funds");
					AFfrChargeAccount.setLayout(null);
					AFfrChargeAccount.setBounds(0,0,500,200);
					//AFfrChargeAccount.setBackground(Color.green);
					AFfrChargeAccount.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
					
					// Back Button
					JButton AFbtnLeaveFundsPage = new JButton("<- Back");
					AFbtnLeaveFundsPage.setBounds(350,100,100,25);
					AFbtnLeaveFundsPage.setCursor(new Cursor(Cursor.HAND_CURSOR));
					AFfrChargeAccount.getContentPane().add(AFbtnLeaveFundsPage);
					
					// $ Label
					JLabel AFmoneylabel = new JLabel("$");
					AFmoneylabel.setBounds(50,100,100,25);
					AFmoneylabel.setFont(new Font("monospaced",Font.BOLD,18));
					AFfrChargeAccount.getContentPane().add(AFmoneylabel);

					// $ Label
					final JLabel lblFavBal = new JLabel("$");
					lblFavBal.setBounds(170,30,200,50);
					lblFavBal.setFont(new Font("monospaced",Font.BOLD,30));
					AFfrChargeAccount.getContentPane().add(lblFavBal);
					
					// $ text box
					final JTextField AF_Funds_box = new JTextField();
					AF_Funds_box.setBounds(75,100,100,25);
					AF_Funds_box.setFont(new Font("sansserif",Font.ITALIC,14));
					AFfrChargeAccount.getContentPane().add(AF_Funds_box);
					
					// Add funds button
					JButton AFbtnAddFunds = new JButton("Add funds");
					AFbtnAddFunds.setBounds(200,100,100,25);
					AFbtnAddFunds.setCursor(new Cursor(Cursor.HAND_CURSOR));
					AFfrChargeAccount.getContentPane().add(AFbtnAddFunds);
					
					//======================================================================//
					                       //Rate Movie //
					//======================================================================//
					
					// Main Frame
					final JFrame frRateMovie = new JFrame("Rate Movie");
					frRateMovie.setLayout(null);
					frRateMovie.setBounds(0,0,700,645);
					//frRateMovie.setBackground(Color.green);
					frRateMovie.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
					
					// Back Button
					JButton btnLeaveRateMoviePage = new JButton("<- Back");
					btnLeaveRateMoviePage.setBounds(100,35,200,25);
					btnLeaveRateMoviePage.setCursor(new Cursor(Cursor.HAND_CURSOR));
					frRateMovie.getContentPane().add(btnLeaveRateMoviePage);
					
					// Choose movie label
					JLabel chooseMovieLabel = new JLabel("Choose a Movie to Rate (by video id)");
					chooseMovieLabel.setBounds(50,400,300,25);
					chooseMovieLabel.setFont(new Font("monospaced",Font.ITALIC,14));
					frRateMovie.getContentPane().add(chooseMovieLabel);
					
					// Choose movie text field
					final JTextField rateMovietext = new JTextField();
					rateMovietext.setBounds(350,400,100,25);
					rateMovietext.setFont(new Font("sansserif",Font.ITALIC,14));
					frRateMovie.getContentPane().add(rateMovietext);
					
					// Choose number of stars label
					JLabel chooseStarsLabel = new JLabel("Choose rating");
					chooseStarsLabel.setBounds(50,425,300,25);
					chooseStarsLabel.setFont(new Font("monospaced",Font.ITALIC,14));
					frRateMovie.getContentPane().add(chooseStarsLabel);
					
					// Choose number of stars text field
					final JTextField chooseRatingText = new JTextField();
					chooseRatingText.setBounds(350,425,100,25);
					chooseRatingText.setFont(new Font("sansserif",Font.ITALIC,14));
					frRateMovie.getContentPane().add(chooseRatingText);
					
					// Place Rating Button
					JButton btnPlaceMovieRating = new JButton("Place Rating");
					btnPlaceMovieRating.setBounds(200,500,200,25);
					btnPlaceMovieRating.setCursor(new Cursor(Cursor.HAND_CURSOR));
					frRateMovie.getContentPane().add(btnPlaceMovieRating);
				/*
					//List Movie Button
					JButton btnListRateMovie = new JButton("List Movies");
					btnListRateMovie.setBounds(300,35,300,25);
					btnListRateMovie.setCursor(new Cursor(Cursor.HAND_CURSOR));
					frRateMovie.getContentPane().add(btnListRateMovie);
					
					// Rate Movie txtSC2
					final JTextArea rateMovietxtSC2 = new JTextArea();
					rateMovietxtSC2.setEditable(false);
					frRateMovie.getContentPane().add(rateMovietxtSC2);
					
					// Rate Movie scpScrollC2
					JScrollPane rateMoviescpScrollC2 = new JScrollPane(rateMovietxtSC2);
					rateMoviescpScrollC2.setBounds(50,100,570,300);
					rateMoviescpScrollC2.setForeground(Color.cyan);
					frRateMovie.getContentPane().add(rateMoviescpScrollC2);
					
					*/
					//====================================================================//
					                          //Add to Favorites//
					//====================================================================//		
					
					// Main Frame
					final JFrame frFavorites = new JFrame("Add To Favorites");
					frFavorites.setLayout(null);
					frFavorites.setBounds(0,0,700,645);
					//frFavorites.setBackground(Color.green);
					frFavorites.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
					
					// Back Button
					JButton btnLeavefavoritesPage = new JButton("<- Back");
					btnLeavefavoritesPage.setBounds(100,35,200,25);
					btnLeavefavoritesPage.setCursor(new Cursor(Cursor.HAND_CURSOR));
					frFavorites.getContentPane().add(btnLeavefavoritesPage);
					
					// Choose movie label
					JLabel chooseFavoriteMovieLabel = new JLabel("Choose a Movie to Favorite (by video id)");
					chooseFavoriteMovieLabel.setBounds(50,300,450,25);
					chooseFavoriteMovieLabel.setFont(new Font("monospaced",Font.ITALIC,14));
					frFavorites.getContentPane().add(chooseFavoriteMovieLabel);
					
					// Choose movie text field
					final JTextField favoriteMovietext = new JTextField();
					favoriteMovietext.setBounds(450,300,100,25);
					favoriteMovietext.setFont(new Font("sansserif",Font.ITALIC,14));
					frFavorites.getContentPane().add(favoriteMovietext);
					
					// write a comment label
					JLabel writeCommentLabel = new JLabel("Write a comment for a movie (by video id)");
					writeCommentLabel.setBounds(50,325,450,25);
					writeCommentLabel.setFont(new Font("monospaced",Font.ITALIC,14));
					frFavorites.getContentPane().add(writeCommentLabel);
					
					// choose video id for comment text
					final JTextField txtvideoIDForComment = new JTextField();
					txtvideoIDForComment.setBounds(450,325,100,25);
					txtvideoIDForComment.setFont(new Font("sansserif",Font.ITALIC,14));
					frFavorites.getContentPane().add(txtvideoIDForComment);
					
					// write a comment text field
					final JTextArea writeCommenttext = new JTextArea();
					//writeCommenttext.setBounds(50,350,300,100);
					writeCommenttext.setFont(new Font("sansserif",Font.ITALIC,14));
					writeCommenttext.setLineWrap(true);
					//frFavorites.getContentPane().add(writeCommenttext);
					
					//scroll for comment
					JScrollPane scpwriteComment = new JScrollPane(writeCommenttext);
					scpwriteComment.setBounds(50,350,300,100);
					scpwriteComment.setForeground(Color.cyan);
					frFavorites.getContentPane().add(scpwriteComment);
				
					// Add to favorites Button
					JButton btnAddToFavorites = new JButton("Add To Favorites");
					btnAddToFavorites.setBounds(100,500,200,25);
					btnAddToFavorites.setCursor(new Cursor(Cursor.HAND_CURSOR));
					frFavorites.getContentPane().add(btnAddToFavorites);
				
					// submit comment button
					JButton btnSubmitComment = new JButton("Submit Comment");
					btnSubmitComment.setBounds(300,500,200,25);
					btnSubmitComment.setCursor(new Cursor(Cursor.HAND_CURSOR));
					frFavorites.getContentPane().add(btnSubmitComment);
					
					/*
					// Main Frame
					final JFrame frFavorites = new JFrame("Add To Favorites");
					frFavorites.setLayout(null);
					frFavorites.setBounds(0,0,700,645);
					frFavorites.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
					
					// Back Button
					JButton btnLeavefavoritesPage = new JButton("<- Back");
					btnLeavefavoritesPage.setBounds(100,35,200,25);
					btnLeavefavoritesPage.setCursor(new Cursor(Cursor.HAND_CURSOR));
					frFavorites.getContentPane().add(btnLeavefavoritesPage);
					
					// Choose movie label
					JLabel chooseFavoriteMovieLabel = new JLabel("Choose a Movie to Favorite (by video id)");
					chooseFavoriteMovieLabel.setBounds(50,400,450,25);
					chooseFavoriteMovieLabel.setFont(new Font("monospaced",Font.ITALIC,14));
					frFavorites.getContentPane().add(chooseFavoriteMovieLabel);
					
					// Choose movie text field
					final JTextField favoriteMovietext = new JTextField();
					favoriteMovietext.setBounds(450,400,100,25);
					favoriteMovietext.setFont(new Font("sansserif",Font.ITALIC,14));
					frFavorites.getContentPane().add(favoriteMovietext);
				
					// Add to favorites Button
					JButton btnAddToFavorites = new JButton("Add To Favorites");
					btnAddToFavorites.setBounds(200,500,200,25);
					btnAddToFavorites.setCursor(new Cursor(Cursor.HAND_CURSOR));
					frFavorites.getContentPane().add(btnAddToFavorites);
				/*
					//List Movie Button
					JButton btnListAllMovies = new JButton("List Movies");
					btnListAllMovies.setBounds(300,35,300,25);
					btnListAllMovies.setCursor(new Cursor(Cursor.HAND_CURSOR));
					frFavorites.getContentPane().add(btnListAllMovies);
					
					// Favorite Movie txtSC2
					final JTextArea favoriteMovietxtSC2 = new JTextArea();
					favoriteMovietxtSC2.setEditable(false);
					frFavorites.getContentPane().add(favoriteMovietxtSC2);
					
					// Favorite Movie scpScrollC2
					JScrollPane favoriteMoviescpScrollC2 = new JScrollPane(favoriteMovietxtSC2);
					favoriteMoviescpScrollC2.setBounds(50,100,570,300);
					favoriteMoviescpScrollC2.setForeground(Color.cyan);
					frFavorites.getContentPane().add(favoriteMoviescpScrollC2);
				*/
					
					//====================================================================//
					                    // See Wall//
					//====================================================================//
					
					
					// Main Frame
					final JFrame frWall = new JFrame("My Wall");
					frWall.setLayout(null);
					frWall.setBounds(0,0,700,850);
					//frWall.setBackground(Color.green);
					frWall.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
					
					// Back Button
					JButton btnLeaveWall = new JButton("<- Back");
					btnLeaveWall.setBounds(400,20,150,25);
					btnLeaveWall.setCursor(new Cursor(Cursor.HAND_CURSOR));
					frWall.getContentPane().add(btnLeaveWall);
					
					// Follow Text
					final JTextField followUserText = new JTextField();
					followUserText.setBounds(75,10,100,25);
					followUserText.setFont(new Font("sansserif",Font.ITALIC,14));
					frWall.getContentPane().add(followUserText);
					
					// Follow Button
					JButton btnFollowUser = new JButton("Follow User");
					btnFollowUser.setBounds(175,10,150,25);
					btnFollowUser.setCursor(new Cursor(Cursor.HAND_CURSOR));
					frWall.getContentPane().add(btnFollowUser);
					
					// UnFollow Text
					final JTextField unfollowUserText = new JTextField();
					unfollowUserText.setBounds(75,35,100,25);
					unfollowUserText.setFont(new Font("sansserif",Font.ITALIC,14));
					frWall.getContentPane().add(unfollowUserText);
					
					// Follow Button
					JButton btnUnfollowUser = new JButton("Unfollow User");
					btnUnfollowUser.setBounds(175,35,150,25);
					btnUnfollowUser.setCursor(new Cursor(Cursor.HAND_CURSOR));
					frWall.getContentPane().add(btnUnfollowUser);

					//
					final JTextArea wallOrdertxtSC = new JTextArea();
					wallOrdertxtSC.setEditable(false);
					frWall.getContentPane().add(wallOrdertxtSC);
					
					// wall Movie scpScrollC1
					JScrollPane wallOrdercpScrollC = new JScrollPane(wallOrdertxtSC);
					wallOrdercpScrollC.setBounds(50,90,570,150);
					wallOrdercpScrollC.setForeground(Color.cyan);
					frWall.getContentPane().add(wallOrdercpScrollC);
					
					//
					final JTextArea wallRatetxtSC = new JTextArea();
					wallRatetxtSC.setEditable(false);
					frWall.getContentPane().add(wallRatetxtSC);
					
					// wall Movie scpScrollC1
					JScrollPane wallRatecpScrollC = new JScrollPane(wallRatetxtSC);
					wallRatecpScrollC.setBounds(50,265,570,150);
					wallRatecpScrollC.setForeground(Color.cyan);
					frWall.getContentPane().add(wallRatecpScrollC);

					//
					final JTextArea wallLikestxtSC = new JTextArea();
					wallLikestxtSC.setEditable(false);
					frWall.getContentPane().add(wallLikestxtSC);
					
					// wall Movie scpScrollC1
					JScrollPane wallLikescpScrollC = new JScrollPane(wallLikestxtSC);
					wallLikescpScrollC.setBounds(50,440,570,150);
					wallLikescpScrollC.setForeground(Color.cyan);
					frWall.getContentPane().add(wallLikescpScrollC);
					
					final JTextArea wallCommentstxtSC = new JTextArea();
					wallCommentstxtSC.setEditable(false);
					frWall.getContentPane().add(wallCommentstxtSC);
					
					// wall Movie scpScrollC1
					JScrollPane wallCommentscpScrollC = new JScrollPane(wallCommentstxtSC);
					wallCommentscpScrollC.setBounds(50,615,570,150);
					wallCommentscpScrollC.setForeground(Color.cyan);
					frWall.getContentPane().add(wallCommentscpScrollC);
					
					//======================================================================//
					                    // Super User Menu //
					//======================================================================//
					
					// Main Frame
					final JFrame frSuperUserMenu = new JFrame("MovieNet -- Super User Home");
					frSuperUserMenu.setLayout(null);
					frSuperUserMenu.setBounds(0,0,700,645);
					//frSuperUserMenu.setBackground(Color.gray);
					frSuperUserMenu.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
					
					// Register Movie Button
					JButton btnSuperRegisterMovie = new JButton("Register Movie");
					btnSuperRegisterMovie.setBounds(265,200,160,25);
					btnSuperRegisterMovie.setCursor(new Cursor(Cursor.HAND_CURSOR));
					frSuperUserMenu.getContentPane().add(btnSuperRegisterMovie);
					
					// Delete User Button
					JButton btnDeleteUser = new JButton("Delete User");
					btnDeleteUser.setBounds(265,260,160,25);
					btnDeleteUser.setCursor(new Cursor(Cursor.HAND_CURSOR));
					frSuperUserMenu.getContentPane().add(btnDeleteUser);
					
					// Delete Comment Button
					JButton btnDeleteComment = new JButton("Delete Comment");
					btnDeleteComment.setBounds(265,320,160,25);
					btnDeleteComment.setCursor(new Cursor(Cursor.HAND_CURSOR));
					frSuperUserMenu.getContentPane().add(btnDeleteComment);
					
					// SQL Console
					JButton btnSQLConsole = new JButton("SQL Console");
					btnSQLConsole.setBounds(265,380,160,25);
					btnSQLConsole.setCursor(new Cursor(Cursor.HAND_CURSOR));
					btnSQLConsole.setMnemonic(KeyEvent.VK_C);
					//btnSQLConsole.setVisible(false);
					frSuperUserMenu.getContentPane().add(btnSQLConsole);
					
					//lblTitle
					JLabel pageTitleSuperUser = new JLabel("MOVIENET MAIN MENU");
					pageTitleSuperUser.setBounds(218,100,300,50);
					pageTitleSuperUser.setFont(new Font("monospaced",Font.BOLD,22));
					pageTitleSuperUser.setForeground(Color.white);
					frSuperUserMenu.getContentPane().add(pageTitleSuperUser);
					
					//btnLogout
					JButton btnSuperUserLogout = new JButton("Logout");
					btnSuperUserLogout.setBounds(520,540,125,25);
					btnSuperUserLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
					frSuperUserMenu.getContentPane().add(btnSuperUserLogout);
					
					//btnLogout
					JButton btnSuperUserLogin = new JButton("User Login");
					btnSuperUserLogin.setBounds(520,500,125,25);
					btnSuperUserLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
					frSuperUserMenu.getContentPane().add(btnSuperUserLogin);
					
					// Super User Background
					JLabel superUserBackgroundImage = new JLabel(new ImageIcon("etc/wallpaper/03.jpg"));
					superUserBackgroundImage.setBounds(0,0,700,645);
					frSuperUserMenu.getContentPane().add(superUserBackgroundImage);
					
					//======================================================================//
								// Super User Register Movie  //
					//======================================================================//
					
					// Main Frame
					final JFrame frRegisterMovie = new JFrame("Register Movie");
					frRegisterMovie.setLayout(null);
					frRegisterMovie.setBounds(0,0,700,645);
					frRegisterMovie.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
					
					// Register Movie Button
					JButton btnRegisterThisMovie = new JButton("Register");
					btnRegisterThisMovie.setBounds(500,40,100,25);
					btnRegisterThisMovie.setCursor(new Cursor(Cursor.HAND_CURSOR));
					frRegisterMovie.getContentPane().add(btnRegisterThisMovie);
					
					// Back Button
					JButton btnLeaveRegisterMovie = new JButton("<- Back");
					btnLeaveRegisterMovie.setBounds(500,75,100,25);
					btnLeaveRegisterMovie.setCursor(new Cursor(Cursor.HAND_CURSOR));
					frRegisterMovie.getContentPane().add(btnLeaveRegisterMovie);
					
					// Title label
					JLabel regMovieTitleLabel = new JLabel("Title");
					regMovieTitleLabel.setBounds(50,40,100,30);
					regMovieTitleLabel.setFont(new Font("serif",Font.BOLD,18));
					regMovieTitleLabel.setVerticalAlignment(SwingConstants.CENTER);
					frRegisterMovie.getContentPane().add(regMovieTitleLabel);
				
					// Title text field
					final JTextField regMovieTitleTxt = new JTextField();
					regMovieTitleTxt.setBounds(250,45,200,25);
					regMovieTitleTxt.setFont(new Font("sansserif",Font.ITALIC,14));
					frRegisterMovie.getContentPane().add(regMovieTitleTxt);
					
					// Year label
					JLabel regMovieYearLabel = new JLabel("Year");
					regMovieYearLabel.setBounds(50,70,100,30);
					regMovieYearLabel.setFont(new Font("serif",Font.BOLD,18));
					regMovieYearLabel.setVerticalAlignment(SwingConstants.CENTER);
					frRegisterMovie.getContentPane().add(regMovieYearLabel);
				
					// Year text field
					final JTextField regMovieYearTxt = new JTextField();
					regMovieYearTxt.setBounds(250,75,200,25);
					regMovieYearTxt.setFont(new Font("sansserif",Font.ITALIC,14));
					frRegisterMovie.getContentPane().add(regMovieYearTxt);
					
					// Online Price Label
					JLabel regMovieOPriceLabel = new JLabel("Online Price");
					regMovieOPriceLabel.setBounds(50,100,150,30);
					regMovieOPriceLabel.setFont(new Font("serif",Font.BOLD,18));
					regMovieOPriceLabel.setVerticalAlignment(SwingConstants.CENTER);
					frRegisterMovie.getContentPane().add(regMovieOPriceLabel);
				
					// Online Price text field
					final JTextField regMovieOPriceTxt = new JTextField();
					regMovieOPriceTxt.setBounds(250,105,200,25);
					regMovieOPriceTxt.setFont(new Font("sansserif",Font.ITALIC,14));
					frRegisterMovie.getContentPane().add(regMovieOPriceTxt);
					
					// DVD Price Label
					JLabel regMovieDVDPriceLabel = new JLabel("DVD Price");
					regMovieDVDPriceLabel.setBounds(50,130,150,30);
					regMovieDVDPriceLabel.setFont(new Font("serif",Font.BOLD,18));
					regMovieDVDPriceLabel.setVerticalAlignment(SwingConstants.CENTER);
					frRegisterMovie.getContentPane().add(regMovieDVDPriceLabel);
					
					// DVD Price text field
					final JTextField regMovieDVDPriceTxt = new JTextField();
					regMovieDVDPriceTxt.setBounds(250,135,200,25);
					regMovieDVDPriceTxt.setFont(new Font("sansserif",Font.ITALIC,14));
					frRegisterMovie.getContentPane().add(regMovieDVDPriceTxt);
					
					// error label
					final JLabel regMovieErrorLabel = new JLabel("");
					regMovieErrorLabel.setBounds(50,370,400,300);
					regMovieErrorLabel.setFont(new Font("serif",Font.BOLD,14));
					regMovieErrorLabel.setVerticalAlignment(SwingConstants.CENTER);
					frRegisterMovie.getContentPane().add(regMovieErrorLabel);
					
					//======================================================================//
					                    // Delete User //
					//======================================================================//
					
					// Main Frame
					final JFrame frDeleteUser = new JFrame("Delete User");
					frDeleteUser.setLayout(null);
					frDeleteUser.setBounds(0,0,500,200);
					frDeleteUser.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
					
					// back button
					JButton btnLeaveDeleteUser = new JButton("<- Back");
					btnLeaveDeleteUser.setBounds(350,75,125,25);
					btnLeaveDeleteUser.setCursor(new Cursor(Cursor.HAND_CURSOR));
					frDeleteUser.getContentPane().add(btnLeaveDeleteUser);
					
					// Delete User Button
					JButton btnDeleteUserNow = new JButton("Delete User");
					btnDeleteUserNow.setBounds(350,40,125,25);
					btnDeleteUserNow.setCursor(new Cursor(Cursor.HAND_CURSOR));
					frDeleteUser.getContentPane().add(btnDeleteUserNow);
					
					
					// Delete this user label
					JLabel delUserLabel = new JLabel("User");
					delUserLabel.setBounds(50,40,100,30);
					delUserLabel.setFont(new Font("serif",Font.BOLD,18));
					delUserLabel.setVerticalAlignment(SwingConstants.CENTER);
					frDeleteUser.getContentPane().add(delUserLabel);
					
					// Delete this user text field
					final JTextField delUserTxt = new JTextField();
					delUserTxt.setBounds(100,45,200,25);
					delUserTxt.setFont(new Font("sansserif",Font.ITALIC,14));
					frDeleteUser.getContentPane().add(delUserTxt);
					
					// error label
					final JLabel delUserErrorLabel = new JLabel("");
					delUserErrorLabel.setBounds(50,370,400,300);
					delUserErrorLabel.setFont(new Font("serif",Font.BOLD,14));
					delUserErrorLabel.setVerticalAlignment(SwingConstants.CENTER);
					frDeleteUser.getContentPane().add(delUserErrorLabel);
					
					//======================================================================//
					                    // Delete Comment //
					//======================================================================//
					
					// Main Frame
					final JFrame frDeleteComment = new JFrame("Delete Comment");
					frDeleteComment.setLayout(null);
					frDeleteComment.setBounds(0,0,500,200);
					frDeleteComment.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
					
					// back button
					JButton btnLeaveDeleteComment = new JButton("<- Back");
					btnLeaveDeleteComment.setBounds(325,75,150,25);
					btnLeaveDeleteComment.setCursor(new Cursor(Cursor.HAND_CURSOR));
					frDeleteComment.getContentPane().add(btnLeaveDeleteComment);
					
					// Delete User Button
					JButton btnDeleteCom = new JButton("Delete comment");
					btnDeleteCom.setBounds(325,40,150,25);
					btnDeleteCom.setCursor(new Cursor(Cursor.HAND_CURSOR));
					frDeleteComment.getContentPane().add(btnDeleteCom);
					
					
					// Delete this user label
					JLabel delCommentLabel = new JLabel("Comment ID");
					delCommentLabel.setBounds(10,40,100,30);
					//delCommentLabel.setFont(new Font("serif",Font.BOLD,18));
					delCommentLabel.setVerticalAlignment(SwingConstants.CENTER);
					frDeleteComment.getContentPane().add(delCommentLabel);
					
					// Delete this user text field
					final JTextField delCommentTxt = new JTextField();
					delCommentTxt.setBounds(100,45,200,25);
					delCommentTxt.setFont(new Font("sansserif",Font.ITALIC,14));
					frDeleteComment.getContentPane().add(delCommentTxt);
					
					// error label
					final JLabel delCommentErrorLabel = new JLabel("");
					delCommentErrorLabel.setBounds(50,370,400,300);
					delCommentErrorLabel.setFont(new Font("serif",Font.BOLD,14));
					delCommentErrorLabel.setVerticalAlignment(SwingConstants.CENTER);
					frDeleteComment.getContentPane().add(delCommentErrorLabel);
					
					//======================================================================//
					                    // SQL Console //
					//======================================================================//
					//Main Frame
					final JFrame frSQLConsole = new JFrame("Register Movie");
					frSQLConsole.setLayout(null);
					frSQLConsole.setBounds(0,0,700,645);
					frSQLConsole.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

					
					// Back Button
					JButton btnLeaveConsolePage = new JButton("<- Back");
					btnLeaveConsolePage.setBounds(100,35,200,25);
					btnLeaveConsolePage.setCursor(new Cursor(Cursor.HAND_CURSOR));
					frSQLConsole.getContentPane().add(btnLeaveConsolePage);
					
					// Choose movie label
					JLabel lblQuery = new JLabel("Enter SQL Query: ");
					lblQuery.setBounds(50,425,150,25);
					lblQuery.setFont(new Font("monospaced",Font.ITALIC,14));
					frSQLConsole.getContentPane().add(lblQuery);
					
					final JTextArea txtQuery = new JTextArea();
					//txtQuery.setBounds(250,425,400,50);
					txtQuery.setLineWrap(true);
					//txtQuery.setFont(new Font("sansserif",Font.ITALIC,14));
					frSQLConsole.getContentPane().add(txtQuery);
					
					JScrollPane scpScrollQuery = new JScrollPane(txtQuery);
					scpScrollQuery.setBounds(200,425,450,50);
					scpScrollQuery.setForeground(Color.cyan);
					frSQLConsole.getContentPane().add(scpScrollQuery);
					
					// 
					JButton btnExecQuery = new JButton("Execute Query");
					btnExecQuery.setBounds(200,500,200,25);
					btnExecQuery.setCursor(new Cursor(Cursor.HAND_CURSOR));
					frSQLConsole.getContentPane().add(btnExecQuery);
									
					// 
					final JTextArea txtSConsole = new JTextArea();
					txtSConsole.setEditable(false);
					frSQLConsole.getContentPane().add(txtSConsole);
					
					// 
					JScrollPane scpScrollConsole = new JScrollPane(txtSConsole);
					scpScrollConsole.setBounds(50,100,600,300);
					scpScrollConsole.setForeground(Color.cyan);
					frSQLConsole.getContentPane().add(scpScrollConsole);
		
					//======================================================================//
										// Button Actions //
					//======================================================================//

					//======================================================================//
										// Login Buttons //
					//======================================================================//
					
					//Verify user and pass. Go from login frame to either superuser or user menu
					btnEnter.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e)
					{
						String user_login = txtUser.getText();
						String user_pass = txtPassword.getText();
	
						if(Login(esql,user_login,user_pass)==1){
							System.out.println("Logged in");
							EmbeddedSQL.currentUser=user_login;
							//JOptionPane.showMessageDialog(frLogin,"Logged In");
							frLogin.setVisible(false);
							frUserMenu.setVisible(true);
							//JOptionPane.showMessageDialog("Logged In");
						}
						else if(Login(esql,user_login,user_pass)==2){
							System.out.println("Welcome Super User.");
							EmbeddedSQL.currentUser=user_login;
							frLogin.setVisible(false);
							frSuperUserMenu.setVisible(true);
							//JOptionPane.showMessageDialog(frLogin,"Super user to implement","Error",JOptionPane.ERROR_MESSAGE);
						}
						else if(Login(esql,user_login,user_pass)==0)
							//System.out.println("Not Logged in");
							JOptionPane.showMessageDialog(frLogin,"User and/or Password Invalid","Error",JOptionPane.ERROR_MESSAGE);
					}
					});
					
					//go from login frame to register frame
					btnRegister.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e)
					{
						frLogin.setVisible(false);
						frRegister.setVisible(true);
						txtUser.setText("");
						txtPassword.setText("");
						txtUsername.setText("");
						RegtxtPassword.setText("");
						reg_fname_txt.setText("");
						reg_mname_txt.setText("");
						reg_lname_txt.setText("");
						reg_email_txt.setText("");
						reg_street1_txt.setText("");
						reg_street2_txt.setText("");
						reg_state_txt.setText("");
						reg_country_txt.setText("");
						reg_zip_txt.setText("");
					}
					});
					
					//Exits the program
					btnExit.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e)
						{
							int reply = JOptionPane.showConfirmDialog(null, "Are you sure yor want to exit?", "Exit", JOptionPane.YES_NO_OPTION);
						    if (reply == JOptionPane.YES_OPTION)
						    {
						    	System.out.print("Disconnecting from database...");
								esql.cleanup ();
								System.out.println("Done\nBye !");
						    	System.exit(0);
						    }
								
						}
					});
					
					//======================================================================//
										// Register Buttons //
					//======================================================================//
					
					//registers a user in the database	
					btnRegOk.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e)
					{
						//boolean regOk = false;
						//int regSQL = 0;
						String uname = txtUsername.getText();
						String upass = RegtxtPassword.getText();
						String ufname = reg_fname_txt.getText();
						String umname = reg_mname_txt.getText();
						String ulname = reg_lname_txt.getText();
						String uemail = reg_email_txt.getText();
						String ustreet1 = reg_street1_txt.getText();
						String ustreet2 = reg_street2_txt.getText();
						String ustate = reg_state_txt.getText();
						String ucountry = reg_country_txt.getText();
						String uzipcode = reg_zip_txt.getText();
						
						String error = "";
						if( uname.length() <= 0 )
							error += "Please enter a username.\n";
						else if( uname.length() >= 10 )
							error += "Please enter a username less than 10 characters.\n";
						else if( upass.length() <= 0 )				
							error += "Please enter a password.\n";
						else if( upass.length() >= 36 )
							error += "Please enter a password less than 36 characters.\n";
						else if( ufname.length() <= 0 )				
							error += "Please enter a first name.\n";
						else if( ufname.length() >= 40 )
							error += "Please enter a first name less than 40 characters.\n";
						else if( ulname.length() <= 0 )				
							error += "Please enter a last name.\n";
						else if( ulname.length() >= 40 )
							error += "Please enter a last name less than 40 characters.\n";
						else if( uemail.length() <= 0 )				
							error += "Please enter an email.\n";
						else if( uemail.length() >= 40 )
							error += "Please enter an email less than 40 characters.\n";
							
						reg_error_label.setText(error);
						
						if(error.length() == 0)
						{
							int balance = 0;
							String query="";
							try{
									query = "INSERT INTO users VALUES ('"+uname+"','"
																		+upass+"','"
																		+ufname+"','"
								   										+umname+"','"
								   										+ulname+"','"
								   										+uemail+"','"
								   										+ustreet1+"','"
								   										+ustreet2+"','"
								   										+ustate+"','"
								   										+ucountry+"','"
								   										+uzipcode+"',"
								   										+balance+");";
									System.out.println("Registering user: " + uname);
									// setting current user
									currentUser = uname;
									JOptionPane.showMessageDialog(frRegister,uname+" Registered");
									
									esql.executeUpdate(query);
									frLogin.setVisible(true);
									frRegister.setVisible(false);
									System.out.println("Registered: " + uname);
							}catch(Exception ev){
								System.err.println (ev.getMessage ());
							}
						}
						
						//txtUser.setText();
						//txtPassword.setText();
					}
					});
					
					//go from register frame to login frame
					btnReturn.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e)
					{
						frLogin.setVisible(true);
						frRegister.setVisible(false);
						txtUser.setText("");
						txtPassword.setText("");
					}
					});

					//====================================================================//
					                    // User Menu Buttons //
					//====================================================================//
					
					//go from user menu frame to login frame
					btnLogout.addActionListener(new ActionListener() {

					public void actionPerformed(ActionEvent e)
					{

						int reply = JOptionPane.showConfirmDialog(null, "Are you sure yor want to LogOut?", "LogOut", JOptionPane.YES_NO_OPTION);
					    if (reply == JOptionPane.YES_OPTION)
					    {
								frLogin.setVisible(true);
								frUserMenu.setVisible(false);
								txtUser.setText("");
								txtPassword.setText("");
						}
					}
					});
					
					//go from user frame to watch movie frame
					btnWatchMovie.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e)
					{
						frWatchMovie.setVisible(true);
						frUserMenu.setVisible(false);
						txtwatchMovie.setText("");
						txtwatchMovie.requestFocus();
						try
						{
							String query ="SELECT video_id,title,online_price,dvd_price FROM video;";
							//String query ="SELECT * FROM users;";
							JTable table1 = new JTable();
							table1 = createTable(query);
							table1.setEnabled(false);
							//table1.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
							JScrollPane watchMoviecpScrollC2 = new JScrollPane(table1);
							watchMoviecpScrollC2.setBounds(50,100,570,300);
							watchMoviecpScrollC2.setForeground(Color.cyan);
							frWatchMovie.getContentPane().add(watchMoviecpScrollC2);
							esql.executeQuery("SELECT balance FROM users WHERE user_id='"+currentUser+"';");
							int balance = rs.getInt(1);
							lblBal2.setText("$ "+balance+".00");
						}catch(Exception ev){
							System.err.println (ev.getMessage ());
						}

					}
					});
					
					// go to add funds page
					btnChargeAccount.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e)
					{
								AFfrChargeAccount.setVisible(true);
								frUserMenu.setVisible(false);
								AF_Funds_box.setText("");
								AF_Funds_box.requestFocus();
						try{						
							esql.executeQuery("SELECT balance FROM users WHERE user_id='"+currentUser+"';");
							int balance = rs.getInt(1);
							lblFavBal.setText("$ "+balance+".00");
						}
						catch(Exception ev)
						{
							System.err.println (ev.getMessage ());
						}		
					}
					});
					

					// go to rate movie page
					btnRateMovie.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e)
					{
							
								frRateMovie.setVisible(true);
								frUserMenu.setVisible(false);
								rateMovietext.setText("");
								chooseRatingText.setText("");
								rateMovietext.requestFocus();
								try
								{
									String query ="SELECT * FROM video;";
									
									JTable table1 = new JTable();
									table1 = createTable(query);
									table1.setEnabled(false);
									table1.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
									JScrollPane rateMoviecpScrollC2 = new JScrollPane(table1);
									rateMoviecpScrollC2.setBounds(50,100,570,300);
									rateMoviecpScrollC2.setForeground(Color.cyan);
									frRateMovie.getContentPane().add(rateMoviecpScrollC2);
									
								}catch(Exception ev){
									System.err.println (ev.getMessage ());
								}
								
					}
					});
					
					
					// go to add to favorites page
					btnAddFavorites.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e)
					{
							
								frFavorites.setVisible(true);
								frUserMenu.setVisible(false);
								
								try
								{
									String query ="SELECT video_id,title FROM video;";
									
									JTable table1 = new JTable();
									table1 = createTable(query);
									table1.setEnabled(false);
									//table1.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
									JScrollPane favoriteMovieScrollC2 = new JScrollPane(table1);
									favoriteMovieScrollC2.setBounds(50,100,570,200);
									favoriteMovieScrollC2.setForeground(Color.cyan);
									frFavorites.getContentPane().add(favoriteMovieScrollC2);
									favoriteMovietext.setText("");
									
								}catch(Exception ev){
									System.err.println (ev.getMessage ());
								}
								
					}
					});
					
					// go to see wall page
					btnSeeWall.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e)
					{	
						frWall.setVisible(true);
						frUserMenu.setVisible(false);	
												
						try
						{
							String orderQuery ="SELECT o.user_id AS order_id, v.title AS title_ordered FROM orders o, video v WHERE user_id=ANY(SELECT user_id_to FROM follow WHERE user_id_from='"+currentUser+"' GROUP BY user_id_to)AND o.video_id=v.video_id;";
							wallOrdertxtSC.setText(getSQL(orderQuery));
							//JTable table2 = new JTable();
							//table2 = createTable(orderQuery);
							//table2.setEnabled(false);
							//table1.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
							//JScrollPane wallOrdercpScrollC = new JScrollPane(table2);
							//wallOrdercpScrollC.setBounds(50,90,570,150);
							//wallOrdercpScrollC.setForeground(Color.cyan);
//frWall.getContentPane().remove(wallOrdercpScrollC);
							//frWall.getContentPane().add(wallOrdercpScrollC);
							
							String rateQuery ="SELECT r.user_id AS user_rating_id, v.title AS title_rated,r.rating FROM rate r,video v WHERE user_id=ANY(SELECT user_id_to FROM follow WHERE user_id_from='"+currentUser+"' GROUP BY user_id_to)AND r.video_id=v.video_id;";			
							wallRatetxtSC.setText(getSQL(rateQuery));
							//JTable table3 = new JTable();
							//table3 = createTable(rateQuery);
							//table3.setEnabled(false);						
							//JScrollPane wallRatecpScrollC = new JScrollPane(table3);
							//wallRatecpScrollC.setBounds(50,265,570,150);
							//wallRatecpScrollC.setForeground(Color.cyan);
//frWall.getContentPane().remove(wallRatecpScrollC);
							//frWall.getContentPane().add(wallRatecpScrollC);
		
							String likeQuery ="SELECT l.user_id AS user_like_id, v.title AS title_liked FROM likes l,video v WHERE user_id=ANY(SELECT user_id_to FROM follow WHERE user_id_from='"+currentUser+"' GROUP BY user_id_to)AND l.video_id=v.video_id;";				
							wallLikestxtSC.setText(getSQL(likeQuery));
							//JTable table4 = new JTable();
							//table4 = createTable(likeQuery);
							//table4.setEnabled(false);						
							//JScrollPane wallLikescpScrollC = new JScrollPane(table4);
							//wallLikescpScrollC.setBounds(50,440,570,150);
							//wallLikescpScrollC.setForeground(Color.cyan);
//frWall.getContentPane().remove(wallLikescpScrollC);
							//frWall.getContentPane().add(wallLikescpScrollC);
							
							String CommentQuery ="SELECT c.user_id AS user_comment_id, v.title AS title_comment, content AS comment FROM comment c,video v WHERE user_id=ANY(SELECT user_id_to FROM follow WHERE user_id_from='"+currentUser+"' GROUP BY user_id_to)AND c.video_id=v.video_id;";				
							wallCommentstxtSC.setText(getSQL(CommentQuery));
							//JTable table5 = new JTable();
							//table5 = createTable(CommentQuery);
							//table5.setEnabled(false);						
							//JScrollPane wallCommnetscpScrollC = new JScrollPane(table5);
							//wallCommnetscpScrollC.setBounds(50,615,570,150);
							//wallCommnetscpScrollC.setForeground(Color.cyan);
//frWall.getContentPane().remove(wallLikescpScrollC);
							//frWall.getContentPane().add(wallCommnetscpScrollC);

						}catch(Exception ev){
							System.err.println (ev.getMessage ());
						}
								
					}
					});
					
					//====================================================================//
					                    // Watch Movie Buttons //
					//====================================================================//
						
					//go back to user menu
					btnWatchReturn.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e)
					{
							frWatchMovie.setVisible(false);
							frUserMenu.setVisible(true);
					}
					});
					
					//adds order on dvd to database
					btnWatchDVD.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e)
					{
						try{
							String str_video_id = txtwatchMovie.getText();
							int video_id = Integer.parseInt(str_video_id);
							int dvd_price=0;
							int balance=0;
							int order_id=0;
							String query="";
							
							try
							{
									if(video_id>=0)
									{				
										int query4 = 0;
										query4=esql.executeQuery("SELECT video_id FROM video WHERE video_id="+video_id+";");
										if(query4 >= 1)
										{
											int query2=esql.executeQuery("SELECT dvd_price FROM video WHERE video_id="+video_id+";");
											dvd_price = rs.getInt(1);
											//System.out.println("dvd_price: "+dvd_price);
											int query3=esql.executeQuery("SELECT balance FROM users WHERE user_id='"+currentUser+"';");
											balance = rs.getInt(1);
											//System.out.println("balance: "+balance);
											int total = -1;
											total = balance-dvd_price;							
											if(total >=0)
											{
												esql.executeUpdate("UPDATE users SET balance="+total+" WHERE user_id='"+currentUser+"';");
												lblBal2.setText("$ "+total+".00");
												int test_order = esql.executeQuery("SELECT MAX(order_id) from orders");
												if(test_order > 0)
												{
													order_id = rs.getInt(1);
													//System.out.println("currOrderId: "+order_id);
													order_id++;
												}

												esql.executeUpdate("INSERT INTO orders VALUES("+order_id+","+video_id+",'"+currentUser+"');");
												int reply = JOptionPane.showConfirmDialog(null, "Your DVD Order has been Placed.\nOrder another movie?", "Confirmation", JOptionPane.YES_NO_OPTION);
												if (reply == JOptionPane.YES_OPTION)
												{
													txtwatchMovie.setText("");
													txtwatchMovie.requestFocus();
												}
												if (reply == JOptionPane.NO_OPTION)
												{
													frUserMenu.setVisible(true);
													frWatchMovie.setVisible(false);
													txtwatchMovie.setText("");
													txtwatchMovie.requestFocus();
												}
											}
											else
											{
												JOptionPane.showMessageDialog(frRateMovie,"Insufficient Funds","Error",JOptionPane.ERROR_MESSAGE);
												txtwatchMovie.setText("");
												txtwatchMovie.requestFocus();
											}
										}
										else
											JOptionPane.showMessageDialog(frRateMovie,"Invalid video ID","Error",JOptionPane.ERROR_MESSAGE);
									}
									else
										JOptionPane.showMessageDialog(frRateMovie,"Enter valid video ID","Error",JOptionPane.ERROR_MESSAGE);
							}catch(Exception ev){
								System.err.println (ev.getMessage ());
							}
						}catch (NumberFormatException ne) {
							JOptionPane.showMessageDialog(frRateMovie,"Invalid video ID","Error",JOptionPane.ERROR_MESSAGE);
						}
					}
					});
					
					//adds order online to database
					btnWatchOnline.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e)
					{
						try{	
							String str_video_id = txtwatchMovie.getText();
							int video_id = Integer.parseInt(str_video_id);
							int online_price=0;
							int balance=0;
							String query="";
								
							try
							{
								if(video_id>=0)
								{
												
									int query4 = 0;
									query4=esql.executeQuery("SELECT video_id FROM video WHERE video_id="+video_id+";");
							
									if(query4 >= 1)
									{
										int query2=esql.executeQuery("SELECT online_price FROM video WHERE video_id="+video_id+";");
										online_price = rs.getInt(1);
										//System.out.println("online_price: "+online_price);
										int query3=esql.executeQuery("SELECT balance FROM users WHERE user_id='"+currentUser+"';");
										balance = rs.getInt(1);
										System.out.println("balance: "+balance);
										int total = -1;
										total = balance-online_price;							
										if(total >=0)
										{
											esql.executeUpdate("UPDATE users SET balance="+total+" WHERE user_id='"+currentUser+"';");
											lblBal2.setText("$ "+total+".00");
											int reply = JOptionPane.showConfirmDialog(null, "Your Online Order has been Placed.\nOrder another movie?", "Confirmation", JOptionPane.YES_NO_OPTION);
											if (reply == JOptionPane.YES_OPTION)
											{
												txtwatchMovie.setText("");
												txtwatchMovie.requestFocus();
											}
											if (reply == JOptionPane.NO_OPTION)
											{
												frUserMenu.setVisible(true);
												frWatchMovie.setVisible(false);
												txtwatchMovie.setText("");
												txtwatchMovie.requestFocus();
											}
										}
										else
										{
											JOptionPane.showMessageDialog(frRateMovie,"Insufficient Funds","Error",JOptionPane.ERROR_MESSAGE);
											txtwatchMovie.setText("");
											txtwatchMovie.requestFocus();
										}
									}
									else
										JOptionPane.showMessageDialog(frRateMovie,"Invalid video ID","Error",JOptionPane.ERROR_MESSAGE);
								}
								else
									JOptionPane.showMessageDialog(frRateMovie,"Enter valid video ID","Error",JOptionPane.ERROR_MESSAGE);
							}catch(Exception ev){
								System.err.println (ev.getMessage ());
							}
						}catch (NumberFormatException ne) {
							JOptionPane.showMessageDialog(frRateMovie,"Invalid video ID","Error",JOptionPane.ERROR_MESSAGE);
						}
					}
					});
					
					//====================================================================//
					                    // Add Funds Buttons //
					//====================================================================//		
					// go back to user menu
					AFbtnLeaveFundsPage.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e)
					{
							
						AFfrChargeAccount.setVisible(false);
						frUserMenu.setVisible(true);
								
					}
					});
					
					// add funds to account in database
					AFbtnAddFunds.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e)
					{
						try
						{
							String balance_string = AF_Funds_box.getText();
							int balance = Integer.parseInt(balance_string);
					try
					{
						if(balance < 0)
							JOptionPane.showMessageDialog(frRateMovie,"Invalid Amount","Error",JOptionPane.ERROR_MESSAGE);
						else
						{
							esql.executeQuery("SELECT balance FROM users WHERE user_id='"+currentUser+"';");
							int currBal = rs.getInt(1);
							int total = -1;
							total = currBal+balance;							
							String query = "UPDATE users SET balance="+total+"WHERE user_id='"+EmbeddedSQL.currentUser+"';";
							esql.executeUpdate(query);
							lblFavBal.setText("$ "+total+".00");
						}
					}catch(Exception ev){
						System.err.println (ev.getMessage ());
					}
						}catch (NumberFormatException ne) {
							JOptionPane.showMessageDialog(frRateMovie,"Invalid Amount","Error",JOptionPane.ERROR_MESSAGE);
						}
					}			
					});
					
					//====================================================================//
					//                   // Rate Movie Buttons //
					//====================================================================//
					
					// go back to logged in menu
					btnLeaveRateMoviePage.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e)
					{
								//rateMovietxtSC2.setText("");
								frRateMovie.setVisible(false);
								frUserMenu.setVisible(true);
								
					}
					});
		
					// Place Rating for Movie and Go Back to logged in menu
					btnPlaceMovieRating.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e)
					{
						try{
							String str_video_id = rateMovietext.getText();
							int video_id = Integer.parseInt(str_video_id);
							String str_rating = chooseRatingText.getText();
							int rating = Integer.parseInt(str_rating);
							String query="";
							try
							{
								int query4 = 0;
								query4=esql.executeQuery("SELECT * FROM video WHERE video_id="+video_id+";");
						
								if(query4 >= 1)
								{
									Timestamp currentTime = new java.sql.Timestamp(Calendar.getInstance().getTime().getTime());
									if( rating > 10 || rating < 1 )
										JOptionPane.showMessageDialog(frRateMovie,"Invalid Rating","Error",JOptionPane.ERROR_MESSAGE);			
									else
									{
										int query5 = esql.executeQuery("SELECT * FROM rate WHERE video_id='"+video_id+"' AND user_id ='"+currentUser+"';");
										if(query5<1)
											query = "INSERT INTO rate VALUES ('"+currentUser+"',"+video_id+",current_timestamp,"+rating+");";
										else
											query ="UPDATE rate SET rating="+rating+"WHERE video_id='"+video_id+"' AND user_id ='"+currentUser+"';";
								//System.out.println("Rating given");
										String query2 = "UPDATE video SET rating="+rating+"WHERE video_id='"+video_id+"';";
										JOptionPane.showMessageDialog(frRateMovie,"video_id: "+video_id+" rating: "+rating);
										//frRateMovie.setVisible(false);
										//frUserMenu.setVisible(true);
										esql.executeUpdate(query2);
										esql.executeUpdate(query);
									}
								}
								else
									JOptionPane.showMessageDialog(frRateMovie,"Invalid video ID","Error",JOptionPane.ERROR_MESSAGE);
							}catch(Exception ev){
								System.err.println (ev.getMessage ());
							}
						}catch (NumberFormatException ne) {
							JOptionPane.showMessageDialog(frRateMovie,"Invalid Entries","Error",JOptionPane.ERROR_MESSAGE);
						}			
					}
					});

					//====================================================================//
					                  // Add To Favorites Button Properties//
					//====================================================================//
					
					// go back to user menu
					btnLeavefavoritesPage.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e)
					{
								frFavorites.setVisible(false);
								frUserMenu.setVisible(true);
								favoriteMovietext.setText("");
								txtvideoIDForComment.setText("");
								writeCommenttext.setText("");
					}
					});
					
					// add a movie to favorites and go back to logged in page
					btnAddToFavorites.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e)
					{
						try{
							String str_favorite_video_id = favoriteMovietext.getText();
							int favorite_video_id = Integer.parseInt(str_favorite_video_id);
							String query = "";
							try
							{
								int query1 = 0;
								query1 = esql.executeQuery("SELECT * FROM video WHERE video_id="+favorite_video_id+";");
								if(query1 >= 1)
								{
									System.out.println("Video id has been found!");
									int query5 = esql.executeQuery("SELECT * FROM likes WHERE video_id='"+favorite_video_id+"' AND user_id ='"+currentUser+"';");
									if(query5<1)
									{
										esql.executeUpdate("INSERT INTO likes VALUES ('"+currentUser+"',"+favorite_video_id+");");
										JOptionPane.showMessageDialog(frFavorites,"The movie have been added to your list.");
										favoriteMovietext.setText("");
										favoriteMovietext.requestFocus();
										txtvideoIDForComment.setText("");
										writeCommenttext.setText("");
										writeCommenttext.repaint();
									}
									else
									{
										JOptionPane.showMessageDialog(frFavorites,"You have already favorited this movie.","Error",JOptionPane.ERROR_MESSAGE);
										favoriteMovietext.setText("");
									}
								}
								else
								{
									JOptionPane.showMessageDialog(frFavorites,"Invalid Video ID","Error",JOptionPane.ERROR_MESSAGE);
									//System.out.println("Error: the video id chosen doesn't exist.");
								}	
							}catch(Exception ev){
								System.err.println (ev.getMessage ());
							}
							}catch (NumberFormatException ne) {
								JOptionPane.showMessageDialog(frRateMovie,"Invalid ID","Error",JOptionPane.ERROR_MESSAGE);
							}			
					}
					});
					
					// write comment to movie and return to logged in page
					btnSubmitComment.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e)
					{
						String str_VideoIDForComment = txtvideoIDForComment.getText();
						int videoIDForComment = Integer.parseInt(str_VideoIDForComment);
						
						String str_getComment = writeCommenttext.getText();
						
						int comment_id = 0;
						
						try
						{
							int query1 = esql.executeQuery("SELECT * FROM video WHERE video_id="+videoIDForComment+";");
							if( query1 >= 1 )
							{
								int numComments = esql.executeQuery("SELECT MAX(comment_id) from comment");
								if( numComments > 0 )
								{
									comment_id=rs.getInt(1);
									comment_id++;
								}
								esql.executeUpdate("INSERT into comment VALUES ("+comment_id+",'"+currentUser+"',"+videoIDForComment+","+"current_timestamp"+",'"+str_getComment+"');");
								JOptionPane.showMessageDialog(frFavorites,"Your Comment has been added.");
								txtvideoIDForComment.setText("");
								writeCommenttext.setText("");
								writeCommenttext.repaint();
							}
							else
							{
								JOptionPane.showMessageDialog(frFavorites,"Invalid Video ID","Error",JOptionPane.ERROR_MESSAGE);
							}	
						}
						catch(Exception ev)
						{
							System.err.println (ev.getMessage ());
						} 		
					}
					});
					
					
					//====================================================================//
					                 //See Wall Button Properties//
					//====================================================================//
					// go back to main menu
					btnLeaveWall.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e)
					{
						followUserText.setText("");
						unfollowUserText.setText("");
						frWall.setVisible(false);
						frUserMenu.setVisible(true);		
					}
					});
					
					// follow user
					btnFollowUser.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e)
					{
						String followThisUser = followUserText.getText();
						
						try
						{
							int query1 = esql.executeQuery("select * from follow where user_id_from = '"+currentUser+ "' AND user_id_to= '"+followThisUser+"';");
							if(query1 < 1)
							{
								esql.executeUpdate("INSERT INTO follow VALUES ('"+followThisUser+"','"+currentUser+"',current_timestamp"+");");
								JOptionPane.showMessageDialog(frWall,"You are following " + followThisUser);
								followUserText.setText("");
							}
							else
							{
								followUserText.setText("");
								JOptionPane.showMessageDialog(frWall,"You are already following this user");	
							}
							
							String orderQuery ="SELECT o.user_id AS order_id, v.title AS title_ordered FROM orders o, video v WHERE user_id=ANY(SELECT user_id_to FROM follow WHERE user_id_from='"+currentUser+"' GROUP BY user_id_to)AND o.video_id=v.video_id;";
							wallOrdertxtSC.setText(getSQL(orderQuery));
							wallOrdertxtSC.repaint();
							
							String rateQuery ="SELECT r.user_id AS user_rating_id, v.title AS title_rated,r.rating FROM rate r,video v WHERE user_id=ANY(SELECT user_id_to FROM follow WHERE user_id_from='"+currentUser+"' GROUP BY user_id_to)AND r.video_id=v.video_id;";			
							wallRatetxtSC.setText(getSQL(rateQuery));
							wallRatetxtSC.repaint();
							
							String likeQuery ="SELECT l.user_id AS user_like_id, v.title AS title_liked FROM likes l,video v WHERE user_id=ANY(SELECT user_id_to FROM follow WHERE user_id_from='"+currentUser+"' GROUP BY user_id_to)AND l.video_id=v.video_id;";				
							wallLikestxtSC.setText(getSQL(likeQuery));
							wallLikestxtSC.repaint();
							
							String CommentQuery ="SELECT c.user_id AS user_comment_id, v.title AS title_comment, content AS comment FROM comment c,video v WHERE user_id=ANY(SELECT user_id_to FROM follow WHERE user_id_from='"+currentUser+"' GROUP BY user_id_to)AND c.video_id=v.video_id;";	
							wallCommentstxtSC.setText(getSQL(CommentQuery));
							wallCommentstxtSC.repaint();
						}
						catch(Exception ev)
						{
							System.err.println (ev.getMessage ());
						}
					}	
					});
					
					// unfollow user
					btnUnfollowUser.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e)
					{
						String unfollowThisUser = unfollowUserText.getText();
						
						try
						{
							int query1 = esql.executeQuery("select * from follow where user_id_from = '"+currentUser+ "' AND user_id_to= '"+unfollowThisUser+"';");
							if(query1 >= 1)
							{
								int reply_unfollow = JOptionPane.showConfirmDialog(null, "Are you sure you want to unfollow this user?", "Unfollow", JOptionPane.YES_NO_OPTION);
				    			if (reply_unfollow == JOptionPane.YES_OPTION)
				    			{
									esql.executeUpdate("DELETE from follow where user_id_from = '"+currentUser+ "' AND user_id_to= '"+unfollowThisUser+"';");
									JOptionPane.showMessageDialog(frWall,"You unfollowed" + unfollowThisUser);
									//unfollowUserText.setText("");
									
									String orderQuery ="SELECT o.user_id AS order_id, v.title AS title_ordered FROM orders o, video v WHERE user_id=ANY(SELECT user_id_to FROM follow WHERE user_id_from='"+currentUser+"' GROUP BY user_id_to)AND o.video_id=v.video_id;";
									wallOrdertxtSC.setText(getSQL(orderQuery));
									wallOrdertxtSC.repaint();
									
									String rateQuery ="SELECT r.user_id AS user_rating_id, v.title AS title_rated,r.rating FROM rate r,video v WHERE user_id=ANY(SELECT user_id_to FROM follow WHERE user_id_from='"+currentUser+"' GROUP BY user_id_to)AND r.video_id=v.video_id;";			
									wallRatetxtSC.setText(getSQL(rateQuery));
									wallRatetxtSC.repaint();
									
									String likeQuery ="SELECT l.user_id AS user_like_id, v.title AS title_liked FROM likes l,video v WHERE user_id=ANY(SELECT user_id_to FROM follow WHERE user_id_from='"+currentUser+"' GROUP BY user_id_to)AND l.video_id=v.video_id;";				
									wallLikestxtSC.setText(getSQL(likeQuery));
									wallLikestxtSC.repaint();
									
									String CommentQuery ="SELECT c.user_id AS user_comment_id, v.title AS title_comment, content AS comment FROM comment c,video v WHERE user_id=ANY(SELECT user_id_to FROM follow WHERE user_id_from='"+currentUser+"' GROUP BY user_id_to)AND c.video_id=v.video_id;";				
									wallCommentstxtSC.setText(getSQL(CommentQuery));
									wallCommentstxtSC.repaint();
									followUserText.setText("");
									unfollowUserText.setText("");
									
				    			}
				    			else
				    			{
				    				followUserText.setText("");
				    				unfollowUserText.setText("");
				    			}
							}
							else
							{
								JOptionPane.showMessageDialog(frWall,"You are already not following this user");
							}
						}
						catch(Exception ev)
						{
							System.err.println (ev.getMessage ());
						}		
					}
					});
					
					//====================================================================//
					                 // Super User Buttons //
					//====================================================================//
					
					//go to normal user menu
					btnSuperUserLogin.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e)
					{
						
							frSuperUserMenu.setVisible(false);
							frUserMenu.setVisible(true);
					}
					});
					
					// log out
					btnSuperUserLogout.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e)
					{
						int reply = JOptionPane.showConfirmDialog(null, "Are you sure yor want to LogOut?", "LogOut", JOptionPane.YES_NO_OPTION);
					    if (reply == JOptionPane.YES_OPTION)
					    {
								frLogin.setVisible(true);
								frSuperUserMenu.setVisible(false);
								txtUser.setText("");
								txtPassword.setText("");
						}
					}
					});
					
					// go to register movie frame
					btnSuperRegisterMovie.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e)
					{
						frSuperUserMenu.setVisible(false);
						frRegisterMovie.setVisible(true);
						regMovieTitleTxt.requestFocus();
					}
					});
					
					// go to delete user frame
					btnDeleteUser.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e)
					{
						frSuperUserMenu.setVisible(false);
						frDeleteUser.setVisible(true);
						delUserTxt.requestFocus();
					}
					});
					
					btnDeleteComment.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e)
					{
							frSuperUserMenu.setVisible(false);
							frDeleteComment.setVisible(true);
					}
					});
					
					//go to SQL console
					btnSQLConsole.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e)
					{
						
							frSuperUserMenu.setVisible(false);
							frSQLConsole.setVisible(true);
							txtQuery.setText("");
							txtSConsole.setText("");
							txtQuery.requestFocus();
					}
					});
					
					
					//====================================================================//
					                    // Register Movie Button //
					//====================================================================//
					
					// go back to super user menu
					btnLeaveRegisterMovie.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e)
					{
						frSuperUserMenu.setVisible(true);
						frRegisterMovie.setVisible(false);
						regMovieTitleTxt.setText("");
						regMovieYearTxt.setText("");
						regMovieOPriceTxt.setText("");
						regMovieDVDPriceTxt.setText("");
					}
					});
					
					// registers movie into database 
					btnRegisterThisMovie.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e)
					{
						try{
							String getMovieTitle = regMovieTitleTxt.getText();
							
							String str_getMovieYear = regMovieYearTxt.getText();
							int movieYear = Integer.parseInt(str_getMovieYear);
							
							String str_getOMoviePrice = regMovieOPriceTxt.getText();
							int oMoviePrice = Integer.parseInt(str_getOMoviePrice);
							
							String str_getMovieDVDPrice = regMovieDVDPriceTxt.getText();
							int dvdMoviePrice = Integer.parseInt(str_getMovieDVDPrice);
							
							String regMovieError = "";
							if( getMovieTitle.length() <= 0 )
								regMovieError += "The movie title you entered is empty.";
							else if( getMovieTitle.length() >= 50 )
								regMovieError += "The Movie title you entered is too long.";
							else if( movieYear < 1800 || movieYear > 2020 )  // these bounds seem reasonable
								regMovieError += "Please enter a correct movie year.";
							else if( oMoviePrice <= 0 )
								regMovieError += "Please enter a online price > 0";
							else if( dvdMoviePrice <= 0 )
								regMovieError += "Please enter a dvd price > 0";
							
							regMovieErrorLabel.setText(regMovieError);
							
							if( regMovieError.length() == 0 )
							{
								try
								{
									int getMaxVideoID = esql.executeQuery("SELECT MAX(video_id) from video");
									int video_id = 0;
									if(getMaxVideoID > 0)
									{
										video_id = rs.getInt(1);
										System.out.println("New video id is: " + (video_id + 1) );
										video_id++;
									}
								
									
									String video_query = "INSERT INTO video VALUES ("
									+video_id+",'"+getMovieTitle+"',"+movieYear+","+oMoviePrice+","+dvdMoviePrice+","
									   										+0+","
									   										+0+","
									   										+"''"+","
									   										+0+");";
									video_id++;
									System.out.println("Registering Movie: " + getMovieTitle);
									JOptionPane.showMessageDialog(frRegisterMovie,getMovieTitle+" Registered");
									esql.executeUpdate(video_query);
									//frSuperUserMenu.setVisible(true);
									//frRegisterMovie.setVisible(false);
									regMovieTitleTxt.setText("");
									regMovieYearTxt.setText("");
									regMovieOPriceTxt.setText("");
									regMovieDVDPriceTxt.setText("");
									regMovieTitleTxt.requestFocus();
									
								}
								catch(Exception ev)
								{
									System.err.println (ev.getMessage ());
								}
							}
						}
						catch (NumberFormatException ne) 
						{
							JOptionPane.showMessageDialog(frRateMovie,"Invalid Entries","Error",JOptionPane.ERROR_MESSAGE);
						}
					}
					});
					
					//====================================================================//
					                 // Delete User Buttons //
					//====================================================================//
					
					// go back to super user menu
					btnLeaveDeleteUser.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e)
					{
						frSuperUserMenu.setVisible(true);
						frDeleteUser.setVisible(false);
						delUserTxt.setText("");
					}
					});
					
					// delete a user
					btnDeleteUserNow.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e)
					{
						String userToDelete = delUserTxt.getText();
						String delUserError = "";
						if( userToDelete.length() > 9 )
						{
							delUserError += "Username invlaid";
						}
						delUserErrorLabel.setText(delUserError);
						
						if( delUserError.length() == 0 )
						{
							try
							{
								int query1 = esql.executeQuery("SELECT * FROM users WHERE user_id = '"+userToDelete+"';");
								if(query1 >= 1)
								{
									esql.executeUpdate("DELETE  from users where user_id = '"+userToDelete+ "';");
									System.out.println("Deleted user: " + userToDelete);
									JOptionPane.showMessageDialog(frSuperUserMenu,userToDelete+" Deleted");
									delUserTxt.setText("");
								}
								else
								{
									JOptionPane.showMessageDialog(frRateMovie,"Invalid user ID","Error",JOptionPane.ERROR_MESSAGE);
									delUserTxt.setText("");
								}
							}
							catch(Exception ev)
							{
								System.err.println (ev.getMessage ());
							}
						}
						//frSuperUserMenu.setVisible(true);
						//frDeleteUser.setVisible(false);
					}
					});
					
					//====================================================================//
					                 // Delete Comment Buttons //
					//====================================================================//
					
					// go back to super user menu
					btnLeaveDeleteComment.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e)
					{
						frSuperUserMenu.setVisible(true);
						frDeleteComment.setVisible(false);
						delCommentTxt.setText("");
					}
					});
					
					// delete a user
					btnDeleteCom.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e)
					{
						String comToDelete = delCommentTxt.getText();
						String delComError = "";
						if( comToDelete.length() > 9 )
						{
							delComError += "Username invlaid";
						}
						delCommentErrorLabel.setText(delComError);
						
						if( delComError.length() == 0 )
						{
							try
							{
								int query1 = esql.executeQuery("SELECT * FROM comment WHERE comment_id = '"+comToDelete+"';");
								if(query1 >= 1)
								{
									esql.executeUpdate("DELETE  from comment where comment_id = '"+comToDelete+ "';");
									System.out.println("Deleted comment: " + comToDelete);
									JOptionPane.showMessageDialog(frSuperUserMenu,comToDelete+" Deleted");
									delCommentTxt.setText("");
								}
								else
								{
									JOptionPane.showMessageDialog(frRateMovie,"Invalid comment ID","Error",JOptionPane.ERROR_MESSAGE);
									delCommentTxt.setText("");
								}
							}
							catch(Exception ev)
							{
								System.err.println (ev.getMessage ());
							}
						}
						//frSuperUserMenu.setVisible(true);
						//frDeleteUser.setVisible(false);
					}
					});
					
					
					//====================================================================//
					                 // SQL Console Buttons //
					//====================================================================//
					
					//executes query
					btnExecQuery.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e)
					{
						String query="";
						query = txtQuery.getText();
						//System.out.println(query);
							try
							{
								txtSConsole.setText(getSQL(query));
								if(getSQL(query).length() == 0)
									txtSConsole.setText("---empty---");
								//System.out.println(getSQL(query));
							}catch(Exception ev)
							{
								System.err.println (ev.getMessage ());
								JOptionPane.showMessageDialog(frRateMovie,ev.getMessage ()+"","Error",JOptionPane.ERROR_MESSAGE);
								txtQuery.setText("");
								txtSConsole.setText("");
								txtQuery.requestFocus();
							}
					}
					});
					
					//go back to super user menu
					btnLeaveConsolePage.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e)
					{
						
							frSuperUserMenu.setVisible(true);
							frSQLConsole.setVisible(false);
					}
					});
				
				}//end run()
			});
		}catch(Exception e){
			System.err.println (e.getMessage ());
			System.out.println("Could not open postgresql");
		}
		finally{
			// make sure to cleanup the created table and close the connection.
			try{
				if(esql != null && keepOn) 
				{
					System.out.print("Disconnecting from database...");
					esql.cleanup ();
					System.out.println("Done\n\nBye !");
				}//end if
			}catch (Exception e){
				// ignored.
			}//end try
		}//end finally
	
	}//end main

   public static int Login(EmbeddedSQL esql,String username, String pw){
	    
	    int user=0;
	    int superuser=0;
	    int access=0;
	    try{
			String query = "select user_id from users where user_id='"+username+"' and password = '"+pw+"';";
			String query2 = "select super_user_id from super_user where super_user_id = '"+username+"';";
		    
		    user = esql.executeQuery (query);
		    superuser = esql.executeQuery (query2);
		    if(user >=1)
		    {
		    	access=1;
		    	if(superuser >=1)
		    		access=2;
		    }
	    }catch(Exception e){
	       System.err.println (e.getMessage ());
	    }
	    return access;
	}


}//end EmbeddedSQL
