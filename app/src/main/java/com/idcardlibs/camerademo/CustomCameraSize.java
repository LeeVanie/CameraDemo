package com.idcardlibs.camerademo;

import android.hardware.Camera;
import android.hardware.Camera.Size;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 自适应 Picturesize和Previewsize CustomCameraSize.java
 * 
 * @Description 是个单例模式类。调用示例：
 * 
 *              Size pictureS =
 *              CustomCameraSize.getInstance().getPictureSize(pictureSizes, 800);
 *              parameters.setPictureSize(pictureS.width, pictureS.height);
 * 
 *              2，程序首先对预览尺寸的list进行升序排序，因为实际中发现，有的手机提供的是升序有的是降序。
 *              3，通过equalRate(Size s, float
 *              rate)保证Size的长宽比率。一般而言这个比率为1.333/1.7777即通常说的4:3和16:9比率。
 * 
 *              4、在getPreviewSize()函数里增加判断if((s.width > th) && equalRate(s,
 *              1.33f))，除保证比率外，还保证用户需要设置的尺寸宽度最小值。这个大家根据需要随便改。
 */
public class CustomCameraSize {

	// private static final String tag = "yan";
	private CameraSizeComparator sizeComparator = new CameraSizeComparator();
	private static CustomCameraSize myCamPara = null;

	private CustomCameraSize() {

	}

	public static CustomCameraSize getInstance() {
		if (myCamPara == null) {
			myCamPara = new CustomCameraSize();
			return myCamPara;
		} else {
			return myCamPara;
		}
	}

	public Size getPreviewSize(List<Size> list, int width, int hight) {
		 float redio;
		 redio = (float)width /(float) hight;
		Collections.sort(list, sizeComparator);

		// for (int i = 0; i < list.size(); i++) {
		// if ((list.get(i).width >= th) && equalRate(list.get(i), 1.33f)) {
		// System.out.println(list.get(i).width + " " + th);
		// return list.get(i);
		// }
		//
		// }
		//
		// return list.get(list.size() - 1);
		List<Size> tempList = new ArrayList<Size>();
		for (Size s : list) {
			if (equalRate(s, redio)) {
				tempList.add(s);
			}
		}
		Size result;
		if (tempList.size() > 0) {
			result = tempList.get(0);
		} else {
			result = list.get(list.size()-1);
		}
		for (Size size : tempList) {
			if (size.width > result.width) {
				result = size;
			}
		}
//		FileLog.methodExXml("333"+result.width+"高"+ result.height+"\n");
		return result;
	}

	public Size getPictureSize(List<Size> list, int width, int hight) {
		
	 float redio;
	 redio = (float)width /(float) hight;
		Collections.sort(list, sizeComparator);

		// int i = 0;
		List<Size> tempList = new ArrayList<Size>();
		for (Size s : list) {
			if (equalRate(s, redio)) {
				tempList.add(s);
			}
			// if((s.width >= th) && equalRate(s, 1.33f)){
			// // Log.i(tag, "最终设置图片尺寸:w = " + s.width + "h = " + s.height);
			// result = list.get(i);
			// }
			// i++;
		}
		Size result;
		if (tempList.size() > 0) {
			result = tempList.get(0);
		} else {
			result = list.get(list.size()-1);
		}
		for (Size size : tempList) {
			if (size.width > result.width) {
				result = size;
			}
		}

		return result;
	}
	
	public Size getPreviewSize(List<Size> list, int width) {
		Collections.sort(list, sizeComparator);

		// for (int i = 0; i < list.size(); i++) {
		// if ((list.get(i).width >= th) && equalRate(list.get(i), 1.33f)) {
		// System.out.println(list.get(i).width + " " + th);
		// return list.get(i);
		// }
		//
		// }
		//
		// return list.get(list.size() - 1);
		List<Size> tempList = new ArrayList<Size>();
		for (Size s : list) {
			if (equalRate(s, 1.77f)) {
				tempList.add(s);
			}
		}
		Size result;
		if (tempList.size() > 0) {
			result = tempList.get(0);
		} else {
			result = list.get(list.size()-1);
		}
		for (Size size : tempList) {
			if (size.width > result.width) {
				result = size;
			}
		}

		return result;
	}

	public Size getPictureSize(List<Size> list, int width) {
		
	 
		Collections.sort(list, sizeComparator);

		// int i = 0;
		List<Size> tempList = new ArrayList<Size>();
		for (Size s : list) {
			if (equalRate(s, 1.77f)) {
				tempList.add(s);
			}
			// if((s.width >= th) && equalRate(s, 1.33f)){
			// // Log.i(tag, "最终设置图片尺寸:w = " + s.width + "h = " + s.height);
			// result = list.get(i);
			// }
			// i++;
		}
		Size result;
		if (tempList.size() > 0) {
			result = tempList.get(0);
		} else {
			result = list.get(list.size()-1);
		}
		for (Size size : tempList) {
			if (size.width > result.width) {
				result = size;
			}
		}

		return result;
	}
	
	public void getPictureAndPreViewSize(Camera.Parameters parameters, int width, int height) {

		 float redio;
		 redio = (float)width /(float) height;
		 Map<String, List<Size>> allSizes = new HashMap<String, List<Size>>();
			String typePreview = "typePreview";
			String typePicture = "typePicture";
			List<Size> listPicture=parameters.getSupportedPictureSizes();
			List<Size> listPreview=parameters.getSupportedPreviewSizes();
			Collections.sort(listPicture, sizeComparator);
			Collections.sort(listPreview, sizeComparator);
			allSizes.put(typePreview, listPreview);
			allSizes.put(typePicture, listPicture);
			
			Iterator iterator = allSizes.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<String, List<Size>> entry = (Map.Entry<String, List<Size>>) iterator.next();
				List<Size> tempList = entry.getValue();
				List<Size> sizes = new ArrayList<Size>();
				for (Size s : tempList) {
					if (equalRate(s, redio)) {
						sizes.add(s);
					}
					// if((s.width >= th) && equalRate(s, 1.33f)){
					// // Log.i(tag, "最终设置图片尺寸:w = " + s.width + "h = " + s.height);
					// result = list.get(i);
					// }
					// i++;
				}
				if (sizes == null || sizes.isEmpty())
					continue;
				ArrayList<WrapCameraSize> wrapCameraSizes = new ArrayList<WrapCameraSize>(sizes.size());
				for (Camera.Size size : sizes) {
					WrapCameraSize wrapCameraSize = new WrapCameraSize();
					wrapCameraSize.setWidth(size.width);
					wrapCameraSize.setHeight(size.height);
					wrapCameraSize.setD(Math.abs((size.width - width)) + Math.abs((size.height - height)));
					if (size.width == width && size.height == height) {
						if (typePreview.equals(entry.getKey())) {
							parameters.setPreviewSize(size.width, size.height);
						} else if (typePicture.equals(entry.getKey())) {
							parameters.setPictureSize(size.width, size.height);
						}
						break;
					}
					wrapCameraSizes.add(wrapCameraSize);
				}
				Camera.Size resultSize = null;
				if (typePreview.equals(entry.getKey())) {
					resultSize = parameters.getPreviewSize();
				} else if (typePicture.equals(entry.getKey())) {
					resultSize = parameters.getPictureSize();
				}
				if (resultSize != null) {
					if (resultSize.width != width && resultSize.height != height) {
						// 找到相机Preview Size 和 Picture Size中最适合的大小
						WrapCameraSize minCameraSize = Collections.min(wrapCameraSizes);
						while (!(minCameraSize.getWidth() >= width && minCameraSize.getHeight() >= height)) {
							wrapCameraSizes.remove(minCameraSize);
							
							if (wrapCameraSizes!=null&&wrapCameraSizes.size()>0) {
								minCameraSize = null;
								minCameraSize = Collections.min(wrapCameraSizes);
							}
							
						}
//						FileLog.methodExXml("2222"+minCameraSize.getWidth()+"高"+ minCameraSize.getHeight()+"\n");
						if (typePreview.equals(entry.getKey())) {
							parameters.setPreviewSize(minCameraSize.getWidth(), minCameraSize.getHeight());
						} else if (typePicture.equals(entry.getKey())) {
							parameters.setPictureSize(minCameraSize.getWidth(), minCameraSize.getHeight());
						}
					}
				}
				iterator.remove();
			}

		}
			

	public boolean equalRate(Size s, float rate) {
		float r = (float) (s.width) / (float) (s.height);
		if (Math.abs(r - rate) <= 0.2) {
			return true;
		} else {
			return false;
		}
	}

	public CameraSizeComparator getSizeComparator() {
		return sizeComparator;
	}


	public  class CameraSizeComparator implements Comparator<Size> {
		// 按升序排列
		public int compare(Size lhs, Size rhs) {
			// TODO Auto-generated method stub
			if (lhs.width == rhs.width) {
				return 0;
			} else if (lhs.width > rhs.width) {
				return 1;
			} else {
				return -1;
			}
		}

	}

}
