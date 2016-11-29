package gui;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import java.awt.Toolkit;

import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JCheckBox;
import javax.swing.JButton;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Color;

public class buyerGui extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	
	private JTextField txtItemName;
	private JTextField txtShopName;
	JCheckBox chckbxHopWorlds = new JCheckBox("hop worlds");
	private JTextField txtMinAmt;
	private JTextField txtGpPerTrip;
	private JTextField txtTrips;
	private JButton Start;
	private JLabel Feather;


	public buyerGui(final ScriptVars var) {
		setTitle("Intelligent feather buyer");
		setIconImage(Toolkit.getDefaultToolkit().getImage(buyerGui.class.getResource("/javax/swing/plaf/metal/icons/ocean/computer.gif")));
		setAlwaysOnTop(true);
		setResizable(false);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 256, 230);
		contentPane = new JPanel();
		contentPane.setBackground(Color.WHITE);
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JPanel panel = new JPanel();
		panel.setBounds(0, 0, 270, 200);
		contentPane.add(panel);
		panel.setLayout(null);
		
		JLabel lblItemName = new JLabel("Item Name:");
		lblItemName.setBounds(0, 11, 93, 14);
		panel.add(lblItemName);
		
		txtItemName = new JTextField();
		txtItemName.setText("Feather pack");
		txtItemName.setBounds(96, 9, 132, 20);
		txtItemName.setEditable(false);
		panel.add(txtItemName);
		txtItemName.setColumns(10);
		
		txtShopName = new JTextField();
		txtShopName.setText("Gerrant");
		txtShopName.setColumns(10);
		txtShopName.setBounds(96, 36, 132, 20);
		txtShopName.setEditable(false);
		panel.add(txtShopName);

		txtGpPerTrip = new JTextField();
		txtGpPerTrip.setText("500000");
		txtGpPerTrip.setBounds(96, 63, 132, 20);
		panel.add(txtGpPerTrip);
		txtGpPerTrip.setColumns(10);

		txtTrips = new JTextField();
		txtTrips.setText("3");
		txtTrips.setBounds(96, 90, 32, 20);
		panel.add(txtTrips);
		txtTrips.setColumns(2);
		
		JLabel lblShopName = new JLabel("Shop name:");
		lblShopName.setBounds(0, 38, 93, 14);
		panel.add(lblShopName);
		
		JLabel lblHopWorlds = new JLabel("Trips");
		lblHopWorlds.setBounds(0, 94, 73, 14);
		panel.add(lblHopWorlds);

		JLabel lblGpPerTrip = new JLabel("Gp/trip:");
		lblGpPerTrip.setBounds(0, 63, 93, 14);
		panel.add(lblGpPerTrip);
		
		chckbxHopWorlds.setBounds(130, 90, 97, 23);
		chckbxHopWorlds.setSelected(true);
		panel.add(chckbxHopWorlds);
		
		JLabel lblMinimumAmt = new JLabel("Min stock:");
		lblMinimumAmt.setBounds(0, 117, 93, 14);
		panel.add(lblMinimumAmt);
		
		txtMinAmt = new JTextField();
		txtMinAmt.setText("88");
		txtMinAmt.setBounds(96, 117, 39, 20);
		panel.add(txtMinAmt);
		txtMinAmt.setColumns(10);
		
		JButton btnNewButton = new JButton("Start!");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				var.shopName = txtShopName.getText();
				var.packName = txtItemName.getText();
				var.Trips = Integer.parseInt(txtTrips.getText());
				var.GpPerTrip = Integer.parseInt(txtGpPerTrip.getText().replace("k","*1000"));
				var.hopWorlds = chckbxHopWorlds.isSelected();
				var.minAmt = Integer.parseInt(txtMinAmt.getText());
				var.started = true;
				dispose();
			}
		});
		btnNewButton.setBounds(0, 154, 250, 34);
		contentPane.add(btnNewButton);
	}
}
