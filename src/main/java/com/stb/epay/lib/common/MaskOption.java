package com.stb.epay.lib.common;

public class MaskOption {
	public static final MaskOption ALL_ASTERISK = of(0, 0, Integer.MAX_VALUE);
	public static final MaskOption CREDIT_CARD = of(4, 4, Integer.MAX_VALUE);
	
	private int leftCharsToKeep;
	private int rightCharsToKeep;
	private int maxCharsToMask;
	private char maskedChar = '*';

	/**
	 * Tạo ra đối tượng MaskOption
	 * 
	 * @param leftCharsToKeep
	 *            số lượng ký tự bên trái (ký tự đầu) không cần mask
	 * @param rightCharsToKeep
	 *            số lượng ký tự bên phải (ký tự cuối) không cần mask
	 * @param maxCharsToMask
	 *            số lượng ký tự tối đa được mask
	 * @param maskedChar
	 *            ký tự dùng để mask
	 * @return
	 */
	public MaskOption(int leftCharsToKeep, int rightCharsToKeep, int maxCharsToMask, char maskedChar) {
		super();
		this.leftCharsToKeep = leftCharsToKeep;
		this.rightCharsToKeep = rightCharsToKeep;
		this.maxCharsToMask = maxCharsToMask;
		this.maskedChar = maskedChar;
	}

	/**
	 * @return số lượng ký tự bên trái (ký tự đầu) không cần mask
	 */
	public int getLeftCharsToKeep() {
		return leftCharsToKeep;
	}

	/**
	 * @return số lượng ký tự bên phải (ký tự cuối) không cần mask
	 */
	public int getRightCharsToKeep() {
		return rightCharsToKeep;
	}

	/**
	 * @return số lượng ký tự tối đa được mask
	 */
	public int getMaxCharsToMask() {
		return maxCharsToMask;
	}

	/**
	 * @return ký tự dùng để mask, mặc định là '*'
	 */
	public char getMaskedChar() {
		return maskedChar;
	}

	
	public MaskOption setLeftCharsToKeep(int leftCharsToKeep) {
		this.leftCharsToKeep = leftCharsToKeep;
		return this;
	}

	public MaskOption setRightCharsToKeep(int rightCharsToKeep) {
		this.rightCharsToKeep = rightCharsToKeep;
		return this;
	}

	public MaskOption setMaxCharsToMask(int maxCharsToMask) {
		this.maxCharsToMask = maxCharsToMask;
		return this;
	}

	public MaskOption setMaskedChar(char maskedChar) {
		this.maskedChar = maskedChar;
		return this;
	}

	/**
	 * Tạo ra đối tượng MaskOption
	 * 
	 * @param leftCharsToKeep
	 *            số lượng ký tự bên trái (ký tự đầu) không cần mask
	 * @param rightCharsToKeep
	 *            số lượng ký tự bên phải (ký tự cuối) không cần mask
	 * @param maxCharsToMask
	 *            số lượng ký tự tối đa được mask
	 * @param maskedChar
	 *            ký tự dùng để mask
	 * @return
	 */
	public static MaskOption of(int leftCharsToKeep, int rightCharsToKeep, int maxCharsToMask, char maskedChar) {
		return new MaskOption(leftCharsToKeep, rightCharsToKeep, maxCharsToMask, maskedChar);
	}

	/**
	 * Tạo ra đối tượng MaskOption, dùng ký tự mask mặc định l
	 * 
	 * @param leftCharsToKeep
	 *            số lượng ký tự bên trái (ký tự đầu) không cần mask
	 * @param rightCharsToKeep
	 *            số lượng ký tự bên phải (ký tự cuối) không cần mask
	 * @param maxCharsToMask
	 *            số lượng ký tự tối đa được mask
	 * @return
	 */
	public static MaskOption of(int leftCharsToKeep, int rightCharsToKeep, int maxCharsToMask) {
		return new MaskOption(leftCharsToKeep, rightCharsToKeep, maxCharsToMask, '*');
	}
}
