import java.awt.EventQueue;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.JTable;
import javax.swing.JButton;
import javax.swing.JComboBox;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
/*
 * Seçilen şehirdeki tüm öğrenciler puan sıralamasına göre sıralanır. Yani, en baştaki öğrencinin şehir sıralaması 1 olur.
 * @author Bengü Demireğen
 * */
public class DershaneDB extends JFrame {
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTable table;
	private JComboBox<String> comboBoxSehir;
	Connection baglanti = null;
	PreparedStatement sorgu;
	ResultSet gelenVeri;
	private JButton btnSirala;

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					DershaneDB frame = new DershaneDB();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public DershaneDB() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 614, 314);
		this.setTitle("ÖĞRENCİ SIRALAMA EKRANI");
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		table = new JTable();
		
		JScrollPane scrollBar = new JScrollPane(table);
		scrollBar.setBounds(10, 11, 578, 217);
		contentPane.add(scrollBar);
		
		btnSirala = new JButton("SIRALA");
		btnSirala.setBounds(10, 239, 89, 23);
		contentPane.add(btnSirala);
		
		// Şehir seçimi için 
		comboBoxSehir = new JComboBox<String>();
		comboBoxSehir.setBounds(120, 239, 150, 23);
		contentPane.add(comboBoxSehir);

		btnSirala.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					Instant start = Instant.now();
					sirala();
					Instant end = Instant.now();

					JOptionPane.showMessageDialog(null, Duration.between(start, end));
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
			}
		});
		
		baglan();
		listele(); // program ilk çalıştığında tablo ekrana gelir.
		sehirleriGetir(); // şehirleri JComboBox'a ekledim
	}

	public void baglan() {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			String url = "jdbc:mysql://localhost:3306/dershane?characterEncoding=utf8";
			String username = "root";
			String password = "";
			baglanti = DriverManager.getConnection(url, username, password);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void listele() {
		 try {
	            sorgu = baglanti.prepareStatement("SELECT * FROM ogrenci");
	            gelenVeri = sorgu.executeQuery();
	            table.setModel(DbUtils.resultSetToTableModel(gelenVeri));
	        } 
		 catch (Exception ex) {
	            JOptionPane.showMessageDialog(null, "Veri listelenirken bir hata oluştu: " + ex.getMessage());
	            ex.printStackTrace();
	        } 
		 finally {
	            try {
	                if (sorgu != null) sorgu.close();
	                if (gelenVeri != null) gelenVeri.close();
	            } catch (SQLException e) {
	                e.printStackTrace();
	            }
	        }
	}

	public void sirala() throws SQLException {
		
		String secilenSehir = (String) comboBoxSehir.getSelectedItem();

        try 
        {
            sorgu = baglanti.prepareStatement("SELECT * FROM ogrenci WHERE sehir = ? ORDER BY puan DESC");
            sorgu.setString(1, secilenSehir);
            gelenVeri = sorgu.executeQuery();

            DefaultTableModel model = DbUtils.resultSetToTableModel(gelenVeri);

            int sehirSiraColumnIndex = -1;
            for (int i = 0; i < model.getColumnCount(); i++) {
                if (model.getColumnName(i).equals("sehir_sira")) {
                    sehirSiraColumnIndex = i;
                    break;
                }
            }
            
            for (int i = 0; i < model.getRowCount(); i++) {
                model.setValueAt(i + 1, i, sehirSiraColumnIndex);
            }

            table.setModel(model);

        } 
        catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Sıralama sırasında bir hata oluştu: " + e.getMessage());
            e.printStackTrace();
        } 
        finally {
            try {
                if (sorgu != null) sorgu.close();
                if (gelenVeri != null) gelenVeri.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
	}

	public void sehirleriGetir() {
		try 
		{
			sorgu = baglanti.prepareStatement("SELECT DISTINCT sehir FROM ogrenci");
			gelenVeri = sorgu.executeQuery();
			while (gelenVeri.next()) 
			{
				comboBoxSehir.addItem(gelenVeri.getString("sehir"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
