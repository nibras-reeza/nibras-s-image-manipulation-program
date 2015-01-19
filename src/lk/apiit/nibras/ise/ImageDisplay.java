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
 * This file represents a JFrame/JInternalFrame used to contain and display an image.
 * Non of the image manipulations are carried out here. Image is transformed into
 * an BufferedImage and passed to this by NIMP. This in turn constructs an object 
 * called PixelImage from the BufferedImage. All image manipulation is delegated
 * to the PixelImage.
 * 
 * References:
 * Amarasinghe, U. (n.d). Basic Effects. [PowerPoint slides]. Colombo: Asia Pacific Institute of Information Technology.
 *		Available at: Learning Management System APIIT City Campus. Imaging and Special Effects. 
 *		<http://lms.apiit.lk/course/view.php?id=1815> (accessed 6th January 2014)
 * Anonymous. n.d. [online] Available at: http://www.apl.jhu.edu/~hall/java/Java2D-Tutorial.html [Accessed: 6 Jan 2014].
 * 
 ************************************************************************************/
package lk.apiit.nibras.ise;

import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.Icon;
import javax.swing.JLabel;

public class ImageDisplay extends JLabel
{
	
	/**
	 * 
	 */
	private static final long	serialVersionUID	= -5083343656596153280L;
	
	private Rectangle			selection;
	
	public ImageDisplay()
	{
		super();
	}
	
	public ImageDisplay(Icon image, int horizontalAlignment)
	{
		super(image, horizontalAlignment);
	}
	
	public ImageDisplay(Icon image)
	{
		super(image);
	}
	
	public ImageDisplay(String text, Icon icon, int horizontalAlignment)
	{
		super(text, icon, horizontalAlignment);
	}
	
	public ImageDisplay(String text, int horizontalAlignment)
	{
		super(text, horizontalAlignment);
	}
	
	public ImageDisplay(String text)
	{
		super(text);
	}
	
	public void setSelection(Rectangle selection)
	{
		this.selection = selection;
		repaint();
	}
	
	public void clearSelection()
	{
		this.selection = null;
		repaint();
	}
	
	public void paintComponent(Graphics g)
	{
		// (Anonymous, n.d.) and (Amarasinghe, n.d.)
		super.paintComponent(g);
		if (selection != null)
			g.drawRect(selection.x, selection.y, selection.width,
					selection.height);
	}
	
}
