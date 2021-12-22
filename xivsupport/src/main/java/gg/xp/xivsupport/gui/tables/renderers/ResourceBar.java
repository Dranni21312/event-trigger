package gg.xp.xivsupport.gui.tables.renderers;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;

public class ResourceBar extends JComponent {

	private final JLabel label = new JLabel("", SwingConstants.CENTER);

	private Color color1;
	private Color color2;
	private Color color3;
	private Color borderColor;
	private Color textColor;
	private double percent1;
	private double percent2;
	private String[] textOptions;

	public ResourceBar() {
		setTextOptions("");
		add(label);
		label.setOpaque(false);
	}

	public void setTextOptions(String... textOptions) {
		if (textOptions.length == 0) {
			throw new IllegalArgumentException("Must specify text");
		}
		this.textOptions = textOptions;
	}

	public Color getColor1() {
		return color1;
	}

	public void setColor1(Color color1) {
		this.color1 = color1;
	}

	public Color getColor2() {
		return color2;
	}

	public void setColor2(Color color2) {
		this.color2 = color2;
	}

	public Color getColor3() {
		return color3;
	}

	public void setColor3(Color color3) {
		this.color3 = color3;
	}

	public Color getBorderColor() {
		return borderColor;
	}

	public void setBorderColor(Color borderColor) {
		this.borderColor = borderColor;
	}

	public double getPercent1() {
		return percent1;
	}

	public void setPercent1(double percent1) {
		this.percent1 = percent1;
	}

	public double getPercent2() {
		return percent2;
	}

	public void setPercent2(double percent2) {
		this.percent2 = percent2;
	}

	private void checkLabel() {
		int width = getWidth();
		for (String text : textOptions) {
			label.setText(text);
			if (label.getPreferredSize().width <= (width - (2 * getBorderWidth()))) {
				break;
			}
		}
		label.setBounds(0, 0, getWidth(), getHeight());
	}

	int getBorderWidth() {
		return borderColor == null ? 0 : 1;
	}

	@Override
	public void revalidate() {
//		super.revalidate();
		checkLabel();
	}

	@Override
	public void validate() {
//		super.validate();
		checkLabel();
	}

	@SuppressWarnings("SuspiciousNameCombination")
	@Override
	protected void paintComponent(Graphics g) {
		AffineTransform old = ((Graphics2D) g).getTransform();
		AffineTransform t = new AffineTransform(old);
		double xScale = t.getScaleX();
		double yScale = t.getScaleY();
		t.scale(1 / xScale, 1 / yScale);
		((Graphics2D) g).setTransform(t);
		int realWidth = (int) Math.floor(getWidth() * xScale);
		if (xScale != 1.0d) {
			realWidth--;
		}
		int realHeight = (int) Math.floor(getHeight() * yScale);
		int borderWidth = getBorderWidth();
		int innerWidth = realWidth - (2 * borderWidth);
		int innerHeight = realHeight - (2 * borderWidth);
		int width1 = (int) (innerWidth * percent1);
		int width2 = (int) (innerWidth * percent2);
		int width3 = innerWidth - width1 - width2;

		if (width1 > 0) {
			g.setColor(color1);
			g.fillRect(borderWidth, borderWidth, width1, innerHeight);
		}

		if (width2 > 0) {
			g.setColor(color2);
			g.fillRect(width1 + borderWidth, borderWidth, width2 + borderWidth, innerHeight);
		}

		if (width3 > 0) {
			g.setColor(color3);
			g.fillRect(width1 + width2 + borderWidth, borderWidth, width3 + borderWidth, innerHeight);
		}

		if (borderColor != null) {
			g.setColor(borderColor);
			g.drawRect(0, 0, realWidth - 1, realHeight - 1);
		}

		((Graphics2D) g).setTransform(old);

//		g.setColor(textColor);
//
//		g.setFont(label.getFont());
//		g.drawString(label.getText(), (width / 2) - (label.getPreferredSize().width / 2), height);

//		label.paintImmediately(g);

//		super.paintChildren(g);
//		super.paintComponent(g);
	}

	public Color getTextColor() {
		return textColor;
	}

	public void setTextColor(Color textColor) {
		this.textColor = textColor;
	}
}
