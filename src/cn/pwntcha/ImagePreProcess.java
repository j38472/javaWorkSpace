package cn.pwntcha;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.IOUtils;

public class ImagePreProcess {
	/**
	 * �ж��ǲ���ɫ�ʶ�С�ڵ���100
	 * @param colorInt
	 * @return
	 */
	public static int isBlack(int colorInt) {
		Color color = new Color(colorInt);
		if (color.getRed() + color.getGreen() + color.getBlue() <= 100) {
			return 1;
		}
		return 0;
	}

	/**
	 * �ж��ǲ���ɫ�ʶȴ���100
	 * @param colorInt
	 * @return
	 */
	public static int isWhite(int colorInt) {
		Color color = new Color(colorInt);
		if (color.getRed() + color.getGreen() + color.getBlue() > 100) {
			return 1;
		}
		return 0;
	}

	/**
	 * ����ͼƬ��ÿ�����ص�
	 * ɾ����ɫ
	 * @param picFile
	 * @return
	 * @throws Exception
	 */
	public static BufferedImage removeBackgroud(String picFile)
			throws Exception {
		BufferedImage img = ImageIO.read(new File(picFile));
		int width = img.getWidth();
		int height = img.getHeight();
		for (int x = 0; x < width; ++x) {
			for (int y = 0; y < height; ++y) {
				if (isWhite(img.getRGB(x, y)) == 1) {
					img.setRGB(x, y, Color.WHITE.getRGB());
				} else {
					img.setRGB(x, y, Color.BLACK.getRGB());
				}
			}
		}
		return img;
	}

	/**
	 * ͼƬ��ȡ
	 * @param img
	 * @return
	 * @throws Exception
	 */
	public static List<BufferedImage> splitImage(BufferedImage img)
			throws Exception {
		List<BufferedImage> subImgs = new ArrayList<BufferedImage>();

		subImgs.add(img.getSubimage(10, 6, 8, 10));
		subImgs.add(img.getSubimage(19, 6, 8, 10));
		subImgs.add(img.getSubimage(28, 6, 8, 10));
		subImgs.add(img.getSubimage(37, 6, 8, 10));
		return subImgs;
	}

	/**
	 * ��ȡtrain ��������ͼ
	 * @return
	 * @throws Exception
	 */
	public static Map<BufferedImage, String> loadTrainData() throws Exception {
		Map<BufferedImage, String> map = new HashMap<BufferedImage, String>();
		File dir = new File("train");
		File[] files = dir.listFiles();
		for (File file : files) {
			map.put(ImageIO.read(file), file.getName().charAt(0) + "");
		}
		return map;
	}

	/**
	 *  ���ָ�õ�ÿһС���������Ա� �����ؽ��
	 * @param img
	 * @param map
	 * @return
	 */
	public static String getSingleCharOcr(BufferedImage img,
			Map<BufferedImage, String> map) {
		String result = "";

		int width = img.getWidth();
		int height = img.getHeight();
		int min = width * height;

		for (BufferedImage bi : map.keySet()) {
			int count = 0;
			Label1: for (int x = 0; x < width; ++x) {
				for (int y = 0; y < height; ++y) {
					if (isWhite(img.getRGB(x, y)) != isWhite(bi.getRGB(x, y))) {
						count++;
						if (count >= min)//������ص㰤������� count ���������
							break Label1;
					}
				}
			}
			if (count < min) {
				min = count;
				result = map.get(bi);
			}
		}
		return result;
	}

	/**
	 * ��ϴ���ͼƬ����һ���ļ�����
	 * @param file
	 * @return
	 * @throws Exception
	 */
	public static String getAllOcr(String file) throws Exception {
		BufferedImage img = removeBackgroud(file);//��ϴͼƬ
//		System.out.println("img:"+img);
		List<BufferedImage> listImg = splitImage(img);//��ȡͼƬ
		Map<BufferedImage, String> map = loadTrainData();//��ȡ���ڶԱȵ�ͼƬ����

		String result = "";
		for (BufferedImage bi : listImg) {
			result += getSingleCharOcr(bi, map);
		}
		ImageIO.write(img, "JPG", new File("result\\"+result+".jpg"));
		return result;
	}

	/**
	 * ����ͼƬ
	 */
	public static void downloadImage() {
		HttpClient httpClient = new HttpClient();
		GetMethod getMethod = new GetMethod(
				"http://www.puke888.com/authimg.php");
		for (int i = 0; i < 30; i++) {
			try {
				Thread.sleep(500);
				// ִ��getMethod
				int statusCode = httpClient.executeMethod(getMethod);
				if (statusCode != HttpStatus.SC_OK) {
					System.err.println("Method failed: "
							+ getMethod.getStatusLine());
				}
				// ��ȡ����
				String picName = "img\\" + i + ".jpg";
				InputStream inputStream = getMethod.getResponseBodyAsStream();
				OutputStream outStream = new FileOutputStream(picName);
				IOUtils.copy(inputStream, outStream);
				outStream.close();
				System.out.println(i+"OK!");
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				// �ͷ�����
				getMethod.releaseConnection();
			}
		}
	}

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
//		downloadImage();
		for (int i = 0; i < 30; ++i) {
			String text = getAllOcr("img\\" + i + ".jpg");
			System.out.println(i + ".jpg = " + text);
		}
	}
}
