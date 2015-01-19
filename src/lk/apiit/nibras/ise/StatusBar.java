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
 * This file represents a status bar that can be used by the main frame to display
 * progress and current status. This contains a setProgress method and a setStatus
 * method to acheive this.
 * 
 * References:
 * No external sources were used.
 * 
 ************************************************************************************/
package lk.apiit.nibras.ise;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public class StatusBar extends JPanel
{
	/**
	 * 
	 */
	private static final long	serialVersionUID	= 5596668328287292464L;
	private JProgressBar		progressBar;
	private JLabel				label;
	
	/**
	 * Create the panel.
	 */
	public StatusBar()
	{
		setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("left:default:grow"),
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("right:default"),
				FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC,
				RowSpec.decode("default:grow"),
				FormFactory.RELATED_GAP_ROWSPEC, }));
		this.label = new JLabel("Nibras's Image Processing Tool");
		add(this.label, "2, 2");
		this.progressBar = new JProgressBar();
		
		add(this.progressBar, "4, 2, right, default");
		
	}
	
	public void setStatus(String status)
	{
		if (status == null || status.equals(""))
			label.setText("Nibras's Image Processing Tool");
		else
			label.setText(status);
		
	}
	
	public void setProgress(int percentage)
	{
		progressBar.setValue(percentage);
	}
	
}
