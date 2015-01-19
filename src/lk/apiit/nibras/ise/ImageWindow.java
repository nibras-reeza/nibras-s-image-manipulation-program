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
 * 	Anonymous. n.d. [online] Available at: http://www.apl.jhu.edu/~hall/java/Java2D-Tutorial.html 
 * 		[Accessed: 6 Jan 2014].
 * 
 *	Burke, D. 2011. image thumbnail question (Swing / AWT / SWT forum at JavaRanch). [online] 
 *		Available at: http://www.coderanch.com/t/559661/GUI/java/image-thumbnail [Accessed: 6 Jan 2014].
 *
 *	Campbell, C. 2007. The Perils of Image.getScaledInstance() | Java.net. [online] 
 *		Available at: https://today.java.net/pub/a/today/2007/04/03/perils-of-image-getscaledinstance.html 
 *		[Accessed: 6 Jan 2014].
 *
 *	Kaving, J. 2012. swing - Maximizing JInternalFrame in Java - Stack Overflow. [online] Available at: 
 *		http://stackoverflow.com/questions/9438035/maximizing-jinternalframe-in-java [Accessed: 6 Jan 2014].
 *
 *	stackoverflow. 2014. java - Add a JScrollPane to a JLabel - Stack Overflow. [online] Available at: 
 *		http://stackoverflow.com/questions/9335138/add-a-jscrollpane-to-a-jlabel [Accessed: 7 Jan 2014].
 *
 *	stackoverflow. 2012. swing - Java ScrollPane on Buffered Image - Stack Overflow. [online] Available at:
 *		 http://stackoverflow.com/questions/7678840/java-scrollpane-on-buffered-image [Accessed: 7 Jan 2014].
 *
 ************************************************************************************/
package lk.apiit.nibras.ise;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

public class ImageWindow extends JInternalFrame
{
	
	// Serialization ID. Not used. Inserted to avoid IDE warnings.
	private static final long	serialVersionUID	= 7321795988754554382L;
	
	// Variables used to display the image.
	// BufferedImage is wrapped inside PixelImage. BufferedImage is
	// is displayed inside a JLabel using an intermediate ImageIcon
	// to support scrolling natively. Other alternative was to create
	// a new image display class that implements Scrollable. This was
	// avoid for simplicity's sake.
	private final ImageDisplay	imageDisplay;
	private final ImageIcon		imageIcon			= new ImageIcon();
	private final PixelImage	pixelImage			= new PixelImage();
	
	private boolean				saved				= true;
	
	// Default constructor is avoided. The internal frame should be
	// strategically sized and positioned to be more user friendly.
	
	public ImageWindow(int x, int y, int height)
	{
		
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addInternalFrameListener(new InternalFrameAdapter()
		{
			@Override
			public void internalFrameClosing(InternalFrameEvent arg0)
			{
				closeWindow();
				
			}
			
		});
		// Make the window resizable by user.
		setResizable(true);
		setIconifiable(true);
		setMaximizable(true);
		setClosable(true);
		
		setBounds(x, y, 450, 300); // Apply size and position.
		
		// Construct a JLabel that displays the ImageIcon.
		imageDisplay = new ImageDisplay(imageIcon);
		
		// Make the image/JLabel scrollable.
		// (stackoverflow, 2013)
		// (stackoverflow, 2012)
		final JScrollPane scroller = new JScrollPane(imageDisplay);
		scroller.setViewportView(imageDisplay);
		getContentPane().add(scroller);
	}
	
	// Allow returning the BufferedImage being displayed in this window.
	// It is not necessary for main app to keep track of which window is
	// displaying which image which would be necessary to support "Save"
	// and "Save As" features.
	public BufferedImage getImage()
	{
		return pixelImage.getImage();
	}
	
	// Load and display a BufferedImage for manipulation.
	public void setImage(BufferedImage image)
	{
		// Update the status bar.
		
		beginOperation("Loading image...");
		
		// Load the image into pixel image.
		pixelImage.setImage(image);
		
		// Resize the window to allow viewing entire image if possible.
		this.setSize(image.getWidth(), image.getHeight());
		
		// Load the image to display.
		imageIcon.setImage(image);
		
		// (Burke, 2011)
		// (Campbell, 2007)
		setFrameIcon(new ImageIcon(image.getScaledInstance(20, 20,
				Image.SCALE_FAST)));
		
		NIMP.getInstance().getStatusBar().setProgress(100);
		beginOperation("");
	}
	
	private void saveImage()
	{
		
		beginOperation("Saving image...");
		
		NIMP.getInstance().handleSave();
		
		updateOperationComplete();
		setSaved(true);
		
		closeWindow();
		
	}
	
	// Default constructor is avoided. The internal frame should be
	// strategically sized and positioned to be more user friendly.
	
	private void closeWindow()
	{
		int response = 0;
		if (!isSaved())
			response = JOptionPane
					.showInternalConfirmDialog(
							this,
							"Are you sure you want to exit? You've made changes to this image which were not saved!",
							"Not saved", JOptionPane.YES_NO_OPTION);
		
		if (response == JOptionPane.YES_OPTION)
			dispose();
		else
			saveImage();
		
	}
	
	// Helper to reset the status bar before starting a task.
	private void beginOperation(String message)
	{
		NIMP.getInstance().getStatusBar().setProgress(0);
		NIMP.getInstance().getStatusBar().setStatus(message);
		
	}
	
	// Helper to clear the status bar and display image once
	// a task is complete.
	private void updateOperationComplete()
	{
		repaint();
		NIMP.getInstance().getStatusBar().setStatus("");
		NIMP.getInstance().getStatusBar().setProgress(100);
		setSaved(false);
	}
	
	// setSize is be overridden to prevent the internal frame from being resized
	// to a size larger than the outer window. This would require the user to
	// to have to move around the window since JDesktopPane doesn't properly
	// support scrolling.
	@Override
	public void setSize(int width, int height)
	{
		// Update the JLabel to the size of the image.
		if (imageDisplay != null)
			imageDisplay.setSize(width, height);
		
		// Get maximum size of window that can be displayed without being cut
		// off.
		// If size being set is larger, just maximize the window without
		// resizing.
		final Dimension size = getToolkit().getScreenSize();
		
		if (width > size.getWidth() - 50 && height > size.getHeight() - 450)
		{
			try
			{
				// (Kaving, 2012)
				setMaximum(true);
			}
			catch (final Exception e)
			{
			}
			return; // Maximized.
		}
		
		// If window was maximized because the size was smaller, call the
		// regular setSize method to resize.
		super.setSize(width, height);
	}
	
	// Image Manipulation related functions.
	// Functions here apply the required manipulation algorithm by
	// delegating it to corresponding function in PixelImage.
	// In addition, updates the status bar and also tries to resize
	// the window if output is likely to be larger than the original
	// image.
	
	public void basicEnlarge(int horizontalPercentage, int verticalPercentage)
	{
		
		beginOperation("Enlarging Image...");
		
		pixelImage.enlarge(horizontalPercentage, verticalPercentage);
		imageIcon.setImage(pixelImage.getImage());
		
		this.setSize(pixelImage.getImage().getWidth(), pixelImage.getImage()
				.getHeight());
		
		updateOperationComplete();
	}
	
	public void negate()
	{
		
		beginOperation("Negating Image...");
		
		pixelImage.negate();
		imageIcon.setImage(pixelImage.getImage());
		
		updateOperationComplete();
		
	}
	
	public void applyAverageFilter()
	{
		
		beginOperation("Smoothing Image...");
		
		pixelImage.applyFilter(PixelImage.FILTERS.AVERAGE_BOX);
		imageIcon.setImage(pixelImage.getImage());
		
		updateOperationComplete();
		
	}
	
	public void applyModeFilter()
	{
		
		beginOperation("Smoothing Image...");
		
		pixelImage.applyModeFilter();
		imageIcon.setImage(pixelImage.getImage());
		
		updateOperationComplete();
		
	}
	
	public void applyKValueFilter(int k)
	{
		
		beginOperation("Smoothing Image...");
		
		pixelImage.applyKValueFilter(k);
		imageIcon.setImage(pixelImage.getImage());
		
		updateOperationComplete();
		
	}
	
	public void applyGaussian1Filter()
	{
		
		beginOperation("Smoothing Image...");
		
		pixelImage.applyFilter(PixelImage.FILTERS.GAUSSIAN_BOX_1);
		imageIcon.setImage(pixelImage.getImage());
		
		updateOperationComplete();
		
	}
	
	public void applyGaussian2Filter()
	{
		
		beginOperation("Smoothing Image...");
		
		pixelImage.applyFilter(PixelImage.FILTERS.GAUSSIAN_BOX_2);
		imageIcon.setImage(pixelImage.getImage());
		
		updateOperationComplete();
		
	}
	
	public void applyLaplaceanLightFilter()
	{
		
		beginOperation("Detecting Edges...");
		
		pixelImage.applyFilter(PixelImage.FILTERS.LAPLACEAN_LIGHT);
		imageIcon.setImage(pixelImage.getImage());
		
		updateOperationComplete();
		
	}
	
	public void applyLaplaceanDarkFilter()
	{
		
		beginOperation("Sharpening Edges...");
		
		pixelImage.applyFilter(PixelImage.FILTERS.LAPLACEAN_DARK);
		imageIcon.setImage(pixelImage.getImage());
		
		updateOperationComplete();
		
	}
	
	public void applyMedianFilter()
	{
		
		beginOperation("Smoothing Image...");
		
		pixelImage.applyFilter(PixelImage.FILTERS.MEDIAN);
		imageIcon.setImage(pixelImage.getImage());
		
		updateOperationComplete();
		
	}
	
	public void applyMedianLowFilter()
	{
		
		beginOperation("Smoothing Image...");
		
		pixelImage.applyFilter(PixelImage.FILTERS.MEDIAN_LOW);
		imageIcon.setImage(pixelImage.getImage());
		
		updateOperationComplete();
		
	}
	
	public void applyLapleanLightMaskOnly()
	{
		
		beginOperation("Highlight Edges...");
		
		pixelImage
				.applyUnweightedMaskAndShowRaw(PixelImage.MASKS.LAPLACEAN_LIGHT);
		imageIcon.setImage(pixelImage.getImage());
		
		updateOperationComplete();
		
	}
	
	public void applyLapleanDarkMaskOnly()
	{
		
		beginOperation("Highlight Edges...");
		
		pixelImage
				.applyUnweightedMaskAndShowRaw(PixelImage.MASKS.LAPLACEAN_DARK);
		imageIcon.setImage(pixelImage.getImage());
		
		updateOperationComplete();
		
	}
	
	public void applyMedianHighFilter()
	{
		
		beginOperation("Smoothing Image...");
		
		pixelImage.applyFilter(PixelImage.FILTERS.MEDIAN_HIGH);
		imageIcon.setImage(pixelImage.getImage());
		
		updateOperationComplete();
		
	}
	
	public void applyUnsharpMasking()
	{
		
		beginOperation("Smoothing Image...");
		
		pixelImage.applyUnsharpMasking();
		imageIcon.setImage(pixelImage.getImage());
		
		updateOperationComplete();
	}
	
	public void applySobelOperator()
	{
		
		beginOperation("Highlighting Edges...");
		
		pixelImage.applySobelOperator();
		imageIcon.setImage(pixelImage.getImage());
		
		updateOperationComplete();
		
	}
	
	public boolean isSaved()
	{
		return saved;
	}
	
	public void setSaved(boolean saved)
	{
		this.saved = saved;
	}
	
	public void adjustBrightness(short amount)
	{
		
		beginOperation("Adjust Brightness...");
		
		pixelImage.adjustBrightness(amount);
		imageIcon.setImage(pixelImage.getImage());
		
		updateOperationComplete();
	}
	
	public void quantizeImage(short stepSize)
	{
		
		beginOperation("Quantizing...");
		
		pixelImage.quantization(stepSize);
		imageIcon.setImage(pixelImage.getImage());
		
		updateOperationComplete();
	}
	
	public void pixellateImage(int tempPixellateRowSize,
			int tempPixellateColSize)
	{
		
		beginOperation("Pixellating...");
		
		pixelImage.pixellate(tempPixellateRowSize, tempPixellateColSize);
		imageIcon.setImage(pixelImage.getImage());
		updateOperationComplete();
	}
	
	public void rotate(double angle)
	{
		
		beginOperation("Rotating...");
		
		pixelImage.rotate(angle);
		imageIcon.setImage(pixelImage.getImage());
		updateOperationComplete();
	}
	
	public void rotateByInterpolate(double angle)
	{
		
		beginOperation("Rotating...");
		
		pixelImage.rotateIngterpolate(angle);
		imageIcon.setImage(pixelImage.getImage());
		updateOperationComplete();
	}
	
	public void undo()
	{
		
		NIMP.getInstance().getStatusBar()
				.setStatus("Restoring last version...");
		
		pixelImage.undo();
		imageIcon.setImage(pixelImage.getImage());
		
		updateOperationComplete();
	}
	
	public void prepUndo()
	{
		
		beginOperation("Saving snapshot...");
		
		pixelImage.prepareUndo();
		
		updateOperationComplete();
	}
	
	public void applySobelMaskOnly()
	{
		
		beginOperation("Highlight Edges...");
		
		pixelImage.applySobelOperatorOnly();
		imageIcon.setImage(pixelImage.getImage());
		
		updateOperationComplete();
		
	}
	
	public void applyPencilSketchUsingSobel(short lightestShade)
	{
		
		NIMP.getInstance().getStatusBar()
				.setStatus("Creating Pencil Sketch...");
		
		pixelImage.convertToPencilSketchUsingSobel(lightestShade);
		imageIcon.setImage(pixelImage.getImage());
		
		updateOperationComplete();
	}
	
	public void applyPencilSketchUsingJinZhou(short lightestShade)
	{
		
		NIMP.getInstance().getStatusBar()
				.setStatus("Creating Pencil Sketch...");
		
		pixelImage.convertToPencilSketchUsingJinZhou(lightestShade);
		imageIcon.setImage(pixelImage.getImage());
		
		updateOperationComplete();
	}
	
	public void applyPencilSketchUsingLaplaceanLight(short lightestShade)
	{
		
		NIMP.getInstance().getStatusBar()
				.setStatus("Creating Pencil Sketch...");
		
		pixelImage.convertToPencilSketchUsingLaplaceanLight(lightestShade);
		imageIcon.setImage(pixelImage.getImage());
		
		updateOperationComplete();
	}
	
	public void applyPencilSketchUsingLaplaceanDark(short lightestShade)
	{
		
		NIMP.getInstance().getStatusBar()
				.setStatus("Creating Pencil Sketch...");
		
		pixelImage.convertToPencilSketchUsingLaplaceanDark(lightestShade);
		imageIcon.setImage(pixelImage.getImage());
		
		updateOperationComplete();
	}
	
	public void applyThresholdToBW128()
	{
		
		NIMP.getInstance().getStatusBar()
				.setStatus("Converting to black and white...");
		
		pixelImage.convertToBWusing128();
		imageIcon.setImage(pixelImage.getImage());
		
		updateOperationComplete();
		
	}
	
	public void applyThresholdToBWOtsu()
	{
		
		NIMP.getInstance().getStatusBar()
				.setStatus("Converting to black and white...");
		
		pixelImage.convertToBWusingOtsu();
		imageIcon.setImage(pixelImage.getImage());
		
		updateOperationComplete();
		
	}
	
	public void applyThresholdOtsu()
	{
		
		NIMP.getInstance().getStatusBar().setStatus("Threshold...");
		
		pixelImage.thresholdUsingOtsu();
		imageIcon.setImage(pixelImage.getImage());
		
		updateOperationComplete();
		
	}
	
	public void applyThreshold128()
	{
		
		NIMP.getInstance().getStatusBar().setStatus("Thresholding...");
		
		pixelImage.thresholdUsing128();
		imageIcon.setImage(pixelImage.getImage());
		
		updateOperationComplete();
		
	}
	
	public void applyConvertToGrayScaleUsingAvg()
	{
		
		NIMP.getInstance().getStatusBar()
				.setStatus("Converting to grayscale...");
		
		pixelImage.convertToGrayScaleUsingAveraging();
		imageIcon.setImage(pixelImage.getImage());
		
		updateOperationComplete();
		
	}
	
	public void applyConvertToGrayScaleUsingLum()
	{
		
		NIMP.getInstance().getStatusBar()
				.setStatus("Converting to grayscale...");
		
		pixelImage.convertToGrayScaleUsingLuminescence();
		imageIcon.setImage(pixelImage.getImage());
		
		updateOperationComplete();
		
	}
	
	public void applyFading(double balance, ImageWindow second)
	{
		
		NIMP.getInstance().getStatusBar().setStatus("Fading");
		
		if (this.getImage().getHeight() < second.getImage().getHeight()
				|| this.getImage().getWidth() < second.getImage().getWidth())
		{
			NIMP.getInstance().showError(
					"Target image must be smaller than the original image!");
			return;
		}
		
		pixelImage.fade(balance, second.pixelImage);
		imageIcon.setImage(pixelImage.getImage());
		
		updateOperationComplete();
		
	}
	
	@Override
	public String toString()
	{
		return (this.getTitle() != null ? this.getTitle() : "");
	}
	
	public void applyTranslate(int vertical, int horizontal)
	{
		
		beginOperation("Translating image...");
		
		pixelImage.translate(vertical, horizontal);
		imageIcon.setImage(pixelImage.getImage());
		
		updateOperationComplete();
		
	}
	
	public void applyContrastEnhancementNaively(int scale)
	{
		
		beginOperation("Enhancing contrast...");
		
		pixelImage.enhanceContrastNaively(scale);
		imageIcon.setImage(pixelImage.getImage());
		
		updateOperationComplete();
		
	}
	
	public void applyContrastEnhancementByStretching()
	{
		
		beginOperation("Enhancing contrast...");
		
		pixelImage.enhanceContrastByStretch();
		imageIcon.setImage(pixelImage.getImage());
		
		updateOperationComplete();
		
	}
	
	public void applyContrastEnhancementByStretching(int max, int min)
	{
		
		beginOperation("Enhancing contrast...");
		
		pixelImage.enhanceContrastByStretch(min, max);
		imageIcon.setImage(pixelImage.getImage());
		
		updateOperationComplete();
		
	}
	
	public void applyContrastEnhancementByEqualization()
	{
		
		beginOperation("Enhancing contrast...");
		
		pixelImage.enhanceContrastUsingHistogramEqualization();
		
		imageIcon.setImage(pixelImage.getImage());
		
		updateOperationComplete();
		
	}
	
	public void handleCrop()
	{
		
		beginOperation("Cropping image...");
		
		final Rectangle cropRect = new Rectangle();
		
		final MouseMotionListener motionAdapter = new MouseMotionAdapter()
		{
			@Override
			public void mouseDragged(MouseEvent e)
			{
				cropRect.setSize(e.getX() - (int) cropRect.getX(), e.getY()
						- (int) cropRect.getY());
				// (Anonymous, n.d.) and (Amarasinghe, n.d.)
				imageDisplay.setSelection(cropRect);
				
			}
		};
		
		final MouseListener mouseAdapter = new MouseAdapter()
		{
			
			@Override
			public void mouseExited(MouseEvent arg0)
			{
				mouseReleased(arg0);
				
			}
			
			@Override
			public void mousePressed(MouseEvent arg0)
			{
				
				if (arg0.getButton() != MouseEvent.BUTTON1)
					return;
				
				cropRect.setLocation(arg0.getX(), arg0.getY());
				
			}
			
			@Override
			public void mouseReleased(MouseEvent arg0)
			{
				int height = (int) cropRect.getHeight();
				int width = (int) cropRect.getWidth();
				
				int x = (int) cropRect.getX();
				
				int y = (int) cropRect.getY();
				
				if (height < 0 || width < 0
						|| (height + y) >= pixelImage.getImage().getHeight()
						|| (width + x) >= pixelImage.getImage().getWidth())
				{
					
					NIMP.getInstance()
							.showError(
									"Crop selection is outside bounds of original image.");
				}
				else
				{
					pixelImage.crop(x, y, height, width);
					imageIcon.setImage(pixelImage.getImage());
					
					ImageWindow.this.setSize(
							pixelImage.getImage().getWidth() + 50, pixelImage
									.getImage().getHeight() + 50);
					
				}
				
				imageDisplay.clearSelection();
				imageDisplay.removeMouseMotionListener(motionAdapter);
				imageDisplay.removeMouseListener(this);
				
				updateOperationComplete();
				
			}
			
		};
		
		imageDisplay.addMouseMotionListener(motionAdapter);
		imageDisplay.addMouseListener(mouseAdapter);
		
	}
	
	public void biLinearEnlarge(int horizontalPercentage, int verticalPercentage)
	{
		
		beginOperation("Enlarging Image...");
		
		pixelImage.enlargeByLinearInterpolate(horizontalPercentage,
				verticalPercentage);
		imageIcon.setImage(pixelImage.getImage());
		
		this.setSize(pixelImage.getImage().getWidth(), pixelImage.getImage()
				.getHeight());
		
		updateOperationComplete();
		
	}
	
	public void applyFishEyeWarp(double factor)
	{
		beginOperation("Warping Image...");
		
		pixelImage.applyFishEyeWarp(factor);
		imageIcon.setImage(pixelImage.getImage());
		
		updateOperationComplete();
	}
	
	public void applyTwirlWarp(double factor)
	{
		beginOperation("Warping Image...");
		
		pixelImage.applyTwirlWarp(factor);
		imageIcon.setImage(pixelImage.getImage());
		
		updateOperationComplete();
	}
	
	public void applyBulgeWarp(double factor)
	{
		beginOperation("Warping Image...");
		
		pixelImage.applyBulgeWarp(factor);
		imageIcon.setImage(pixelImage.getImage());
		
		updateOperationComplete();
	}
	
}
