/*************************************************************************************
 * 
 * Author: Nibras Ahamed Reeza (CB004641)
 * Email: nibras.me@facebook.com
 * 
 * Created: 21st of December, 2013
 * 
 * This file part of the image processing artifact created for Imaging and Special
 * Effects module.
 * 
 * This file represents a simple window used as the About window. It is a very simple 
 * window with a timer to fade it away with time.
 * 
 * References:
 * Skeleton made available in the class was used to position the window in the center
 * of the screen. 
 * 
 ************************************************************************************/

package lk.apiit.nibras.ise;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.MatteBorder;

public class AboutWindow extends JFrame
{
	float						fade				= 1.0f;
	/**
	 * 
	 */
	private static final long	serialVersionUID	= 7671334280230065042L;
	private JPanel				contentPane;
	private JPanel				panel;
	private JLabel				lblNibrass;
	private JLabel				lblImageManipulationProgram;
	private JButton				btnClose;
	private JButton				btnUserGuide;
	private JLabel				lblX;
	private JTextArea			txtrVersion;
	private JLabel				lblImageCredits;
	
	/**
	 * Create the frame.
	 */
	public AboutWindow()
	{
		Timer timer = new Timer(0, new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				AboutWindow.this.dispose();
				
			}
		});
		// Following code snippet was extracted from
		// Claude C. Chibelushi's code samples distributed
		// in class by Mr. Udesh Amarasinghe.
		// (Chibelushi 2009)
		final Dimension screenSize = getToolkit().getScreenSize();
		this.setLocation((screenSize.width - 500) / 2,
				(screenSize.height - 350) / 2);
		
		setUndecorated(true);
		
		setAlwaysOnTop(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(500, 350);
		this.contentPane = new JPanel();
		this.contentPane.setBackground(Color.WHITE);
		this.contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		this.contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(this.contentPane);
		this.panel = new JPanel();
		this.panel.setBorder(new EtchedBorder(EtchedBorder.RAISED, null, null));
		this.panel.setBackground(Color.WHITE);
		this.contentPane.add(this.panel, BorderLayout.CENTER);
		this.panel.setLayout(null);
		this.lblImageCredits = new JLabel(
				"Image Credits: A lioness roars in Ngorongoro Conservation Area, Tanzania (SajjadF 2010)");
		this.lblImageCredits.setFont(new Font("Tahoma", Font.PLAIN, 10));
		this.lblImageCredits.setBounds(10, 315, 470, 14);
		this.panel.add(this.lblImageCredits);
		this.txtrVersion = new JTextArea();
		this.txtrVersion.setFont(new Font("Monospaced", Font.PLAIN, 10));
		this.txtrVersion.setMargin(new Insets(4, 4, 4, 4));
		this.txtrVersion.setEditable(false);
		this.txtrVersion
				.setText("v1.0\r\nDeliverable for Imaging and Special Effects,\r\nin partial fullfilment of requirements for,\r\nBEng. (Hons) Computing Science.\r\n\r\nSupervised by: Mr. Udesh Amarasinghe");
		this.txtrVersion.setBounds(174, 215, 290, 89);
		this.panel.add(this.txtrVersion);
		this.lblNibrass = new JLabel("Nibras's");
		this.lblNibrass.setFont(new Font("Brush Script Std", Font.BOLD
				| Font.ITALIC, 60));
		this.lblNibrass.setBounds(24, 46, 456, 77);
		this.panel.add(this.lblNibrass);
		this.lblImageManipulationProgram = new JLabel(
				"Image Manipulation Program");
		this.lblImageManipulationProgram.setFont(new Font("Brush Script Std",
				Font.PLAIN, 32));
		this.lblImageManipulationProgram.setBounds(24, 134, 446, 38);
		this.panel.add(this.lblImageManipulationProgram);
		this.btnClose = new JButton("Close");
		this.btnClose.setFont(new Font("Calibri", Font.PLAIN, 16));
		this.btnClose
				.setBorder(new MatteBorder(3, 3, 3, 3, new Color(0, 0, 0)));
		this.btnClose.setMnemonic(KeyEvent.VK_L);
		this.btnClose.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0)
			{
				AboutWindow.this.disposeHelper();
			}
		});
		this.btnClose.setBounds(24, 266, 106, 38);
		this.panel.add(this.btnClose);
		this.btnUserGuide = new JButton("User Guide");
		this.btnUserGuide.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				NIMP.getInstance().handleShowDoc();
			}
		});
		this.btnUserGuide.setFont(new Font("Calibri", Font.PLAIN, 16));
		this.btnUserGuide.setBorder(new MatteBorder(3, 3, 3, 3, new Color(0, 0,
				0)));
		this.btnUserGuide.setBounds(24, 217, 106, 38);
		this.panel.add(this.btnUserGuide);
		this.lblX = new JLabel("X");
		this.lblX.setIcon(new ImageIcon(AboutWindow.class
				.getResource("/lk/apiit/nibras/ise/Lion_Serengeti.JPG")));
		this.lblX.setBounds(10, 11, 470, 318);
		this.panel.add(this.lblX);
		this.setVisible(true);
		
		timer.setInitialDelay(7000);
		timer.setRepeats(false);
		timer.start();
		
	}
	
	public void disposeHelper()
	{
		super.dispose();
	}
	
	public void dispose()
	{
		new Timer(10, new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				if (fade <= 0.0f)
				{
					AboutWindow.this.disposeHelper();
					return;
				}
				
				AboutWindow.this.setOpacity(fade);
				fade -= 0.005f;
				
			}
		}).start();
		
	}
}
