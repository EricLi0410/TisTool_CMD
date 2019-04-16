package com.qqing;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.CellStyle;


public class TisTool {
	//mysql infomation
	static final String driver = "com.mysql.cj.jdbc.Driver";
	static final String url = "jdbc:mysql://10.59.216.88:12783/document?useUnicode=true&characterEncoding=utf8&serverTimezone=GMT%2B8&useSSL=false&zeroDateTimeBehavior=convertToNull";
	static final String username = "slaveroot";
	static final String password = "sQ9Zs11^Sk";
	
	static long counter;
	
	//register the connection
	public static void registerConn() {
		try{
			Class.forName(driver);  //加载驱动程序，此处运用隐式注册驱动程序的方法
		}catch(ClassNotFoundException e){
			e.printStackTrace();
		}
	}
	
	// search infomation
	public static void search() throws IOException, SQLException {
		Connection con = DriverManager.getConnection(url,username,password);
		Statement st = con.createStatement();  //创建sql执行对象
		Boolean flag = false;
		List<Document> docLs = new ArrayList<>();
//		String queryCategory = "select id,title from cDocCategoryBasicIntl where pid = 0";
		String queryCategory = "select id,title from cDocCategoryBasic where pid = 0";
		
		List<Map<String,Object>> categoryLs = new ArrayList<>();
		ResultSet categoryRs = st.executeQuery(queryCategory);  //执行sql语句并返回结果集
		while(categoryRs.next()) {  //遍历结果集进行处理
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("id", categoryRs.getInt("id"));
			map.put("title", categoryRs.getString("title"));
			categoryLs.add(map);
		}
		if(categoryRs != null){  //关闭对象
			try{
				categoryRs.close();
			}catch(SQLException e){
				e.printStackTrace();
			}
		}
		
		for (Map<String, Object> categoryMap : categoryLs) {
			System.out.println("category:"+categoryMap.get("id"));
			String basicId = categoryMap.get("id").toString();  //文档所属分类Id
			String firstMenu = categoryMap.get("title").toString();  //获取文档所属分类作为一级菜单
//			String queryProduct = "select id,title from cDocCategoryBasicIntl where pid = " + basicId;
			String queryProduct = "select id,title from cDocCategoryBasic where pid = " + basicId;
			List<Map<String,Object>> productLs = new ArrayList<>();
			ResultSet productRs = st.executeQuery(queryProduct);
			while(productRs.next()) {  //遍历结果集
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("id", productRs.getInt("id"));
				map.put("title", productRs.getString("title"));
				productLs.add(map);
			}
			if(productRs != null){  //关闭对象
				try{
					productRs.close();
				}catch(SQLException e){
					e.printStackTrace();
				}
			}
			for(Map<String, Object> productMap : productLs) {
				Integer categoryId = (Integer) productMap.get("id");  //文档所属产品Id
				String product = productMap.get("title").toString();  //获取文档产品名称
				String secondMenu = productMap.get("title").toString();  //获取文档所属产品作为二级菜单
			/*	String queryByProduct = "SELECT DISTINCT t2.id as page, t1.pid, t2.lang, t2.title,t2.recentReleaseTime,"
						+ "t2.sourceUrl,t1.source, t1.disable, t1.redirectUrl  "
						+ "from cDocPageBasicIntl t1 , cDocPageDetailIntl t2 , cDocCategoryBasicIntl t3 "
						+ "where t2.id=t1.id and t1.categoryId=t3.id  and t1.type='page' and t1.disable=0 "
						+ "and t1.redirectUrl='' and t1.categoryId="+categoryId ;   */
				String queryByProduct = "SELECT DISTINCT t2.id as page, t1.pid, t2.lang, t2.title,t2.recentReleaseTime,"
						+ "t2.sourceUrl,t1.source, t1.disable, t1.redirectUrl  "
						+ "from cDocPageBasic t1 , cDocPageDetail t2 , cDocCategoryBasic t3 "
						+ "where t2.id=t1.id and t1.categoryId=t3.id  and t1.type='page' and t1.disable=0 "
						+ "and t1.redirectUrl='' and t1.categoryId="+categoryId ;
				List<Map<String,Object>> docInfo = new ArrayList<>();
				ResultSet docRs = st.executeQuery(queryByProduct);
				while(docRs.next()) {
					Map<String, Object> map = new HashMap<String, Object>();
					map.put("page", docRs.getInt("page"));
					map.put("pid", docRs.getInt("pid"));
					map.put("lang", docRs.getString("lang"));
					map.put("title", docRs.getString("title"));
					
					Date rrt = docRs.getTimestamp("recentReleaseTime");
					if(rrt != null) {
						map.put("recentReleaseTime", rrt);
					}else {
						map.put("recentReleaseTime",null);
					}
					map.put("sourceUrl", docRs.getString("sourceUrl"));
					map.put("source", docRs.getString("source"));
					map.put("disable", docRs.getInt("disable"));
					map.put("redirectUrl", docRs.getString("redirectUrl"));
					docInfo.add(map);
				}
				if(docRs != null){  //关闭对象
					try{
						docRs.close();
					}catch(SQLException e){
						e.printStackTrace();
						System.out.println(e);
					}
				}
				
				for(Map<String, Object> docMap : docInfo) {
					Document docmt = new Document();  //初始化实体类对象
					docmt.setProduct(product);
					docmt.setFirstMenu(firstMenu);
					docmt.setSecondMenu(secondMenu);
					docmt.setTitle(docMap.get("title").toString()); //文档标题
					docmt.setLang(docMap.get("lang").toString());  //文档语言
					docmt.setGithubUrl(docMap.get("sourceUrl").toString()); //github链接
					docmt.setSource(docMap.get("source").toString()); //来源
					Integer page = (Integer) docMap.get("page");  //文档页面id
//					docmt.setWebUrl("intl.cloud.tencent.com/document/product/"+categoryId+"/"+page);
					docmt.setWebUrl("cloud.tencent.com/document/product/"+categoryId+"/"+page);
					
					Date releaseTime = (Date) docMap.get("recentReleaseTime");  //文档发布时间
					if(releaseTime == null) {
						docmt.setIfRelease("未发布");
					}else{
						docmt.setReleaseTime(releaseTime);
//						String queryStatus = "select inMenu,visible from cDocPageStatusIntl where id ="+page+" and lang = \""+docmt.getLang()+"\";";
						String queryStatus = "select inMenu,visible from cDocPageStatus where id ="+page+" and lang = \""+docmt.getLang()+"\";";
						
						List<Map<String,Object>> statusLs = new ArrayList<>();
						ResultSet statusRs = st.executeQuery(queryStatus);
						while(statusRs.next()) {
							Map<String, Object> map = new HashMap<String, Object>();
							map.put("inMenu", statusRs.getInt("inMenu"));
							map.put("visible", statusRs.getInt("visible"));
							statusLs.add(map);
						}
						if(statusRs != null){  //关闭对象
							try{
								statusRs.close();
							}catch(SQLException e){
								e.printStackTrace();
								System.out.println(e);
							}
						}
						
						for(Map<String, Object> statusMap : statusLs) {
							Integer inMenu = (Integer) statusMap.get("inMenu");
							Integer visible = (Integer) statusMap.get("visible");
							if(inMenu == 1 && visible == 1) {
								docmt.setIfRelease("已发布");
								Integer pid = (Integer) docMap.get("pid");   //获取文档父级菜单
								List<String> mList = new ArrayList<>();
								while(pid != 0) {
//									String sql2 = "select id,pid,title from cDocPageBasicIntl where id = " + pid;
									String sql2 = "select id,pid,title from cDocPageBasic where id = " + pid;
									
									List<Map<String,Object>> list = new ArrayList<>();
									ResultSet pRs = st.executeQuery(sql2);
									while(pRs.next()) {
										Map<String, Object> map = new HashMap<String, Object>();
										map.put("id", pRs.getInt("id"));
										map.put("pid", pRs.getInt("pid"));
										map.put("title", pRs.getString("title"));
										list.add(map);
									}
									if(pRs != null){  //关闭对象
										try{
											pRs.close();
										}catch(SQLException e){
											e.printStackTrace();
										}
									}
									String menu = "";
									for (Map<String, Object> map : list) {
										menu = map.get("title").toString();
										pid = (Integer) map.get("pid");
									}
									mList.add(menu);
								}
								// 处理菜单名称
								if(mList.size() == 1) {
									docmt.setThirdMenu(mList.get(0));
									docmt.setFourthMenu("");
									docmt.setFiveMenu("");
								}else if(mList.size() == 2) {
									docmt.setFiveMenu("");
									docmt.setFourthMenu(mList.get(0));
									docmt.setThirdMenu(mList.get(1));
								}
								else if(mList.size() == 3) {
									docmt.setFiveMenu(mList.get(0));
									docmt.setFourthMenu(mList.get(1));
									docmt.setThirdMenu(mList.get(2));
								}
							}else if(inMenu == 0 && visible == 1 ){
								docmt.setIfRelease("不可见");
								docmt.setThirdMenu("");
								docmt.setFourthMenu("");
								docmt.setFiveMenu("");
							}else if(inMenu == 1 && visible == 0 ){
								docmt.setIfRelease("不可见");
								docmt.setThirdMenu("");
								docmt.setFourthMenu("");
								docmt.setFiveMenu("");
							}else if(inMenu == 0 && visible == 0){
								docmt.setIfRelease("不存在");
								docmt.setThirdMenu("");
								docmt.setFourthMenu("");
								docmt.setFiveMenu("");
							}
						}
					}
					docLs.add(docmt);
					counter += counter;
				}
				int size = docLs.size();
				if(size != 0) {
					excelPoi(docLs, flag);
					System.out.println("docls Size:"+docLs.size());
				}
				docLs.clear();
				flag = true;
			}
			
		}
		if(con != null){
			try{
				con.close();
			}catch(SQLException e){
				e.printStackTrace();
				System.out.println(e);
			}
		}
	}
	
	// export excel
	public static Boolean excelPoi (List<Document> list , Boolean flag) throws IOException {
		HSSFWorkbook workbook = null;
		HSSFSheet sheet = null;
		int rowIndex = 0;
		if(flag == false) {
			workbook = new HSSFWorkbook();
			sheet = workbook.createSheet("全量映射信息");
			sheet.setDefaultColumnWidth(17);
			// 创建列表名称样式
	        HSSFCellStyle headerStyle = workbook.createCellStyle();
	        headerStyle.setWrapText(true);// 设置长文本自动换行
	        headerStyle.setAlignment(CellStyle.ALIGN_CENTER);//居中
	 
	        // 创建表头，列
	        HSSFRow headerRow = sheet.createRow(0);
	        headerRow.setHeightInPoints(20f);
	 
	        HSSFCell header1 = headerRow.createCell(0);//0列
	        header1.setCellValue("产品");
	        header1.setCellStyle(headerStyle);
	        HSSFCell header2 = headerRow.createCell(1);//1列
	        header2.setCellValue("产品标题");
	        header2.setCellStyle(headerStyle);
	        HSSFCell header3 = headerRow.createCell(2);
	        header3.setCellValue("语言");
	        header3.setCellStyle(headerStyle);
	        HSSFCell header4 = headerRow.createCell(3);
	        header4.setCellValue("是否发布");
	        header4.setCellStyle(headerStyle);
	        HSSFCell header5 = headerRow.createCell(4);
	        header5.setCellValue("发布时间");
	        header5.setCellStyle(headerStyle);
	        HSSFCell header6 = headerRow.createCell(5);
	        header6.setCellValue("一级菜单名称");
	        header6.setCellStyle(headerStyle);
	        HSSFCell header7 = headerRow.createCell(6);
	        header7.setCellValue("二级菜单名称");
	        header7.setCellStyle(headerStyle);
	        HSSFCell header8 = headerRow.createCell(7);
	        header8.setCellValue("三级菜单名称");
	        header8.setCellStyle(headerStyle);
	        HSSFCell header9 = headerRow.createCell(8);
	        header9.setCellValue("四级菜单名称");
	        header9.setCellStyle(headerStyle);
	        HSSFCell header10 = headerRow.createCell(9);
	        header10.setCellValue("五级菜单名称");
	        header10.setCellStyle(headerStyle);
	        HSSFCell header11 = headerRow.createCell(10);
	        header11.setCellValue("官网链接");
	        header11.setCellStyle(headerStyle);
	        HSSFCell header12 = headerRow.createCell(11);
	        header12.setCellValue("github链接");
	        header12.setCellStyle(headerStyle);
	        HSSFCell header13 = headerRow.createCell(12);
	        header13.setCellValue("来源");
	        header13.setCellStyle(headerStyle);
	        
	        rowIndex = 1;
		}else {
//			FileInputStream fs=new FileInputStream("/data/gitUrlIntl.xls");
			FileInputStream fs=new FileInputStream("/data/gitUrlCh.xls"); //文件保存路径
			POIFSFileSystem ps=new POIFSFileSystem(fs);  //使用POI提供的方法得到excel的信息
			workbook=new HSSFWorkbook(ps); 
			sheet = workbook.getSheet("全量映射信息");
			int startRow = sheet.getLastRowNum() + 1;  //追加开始行数
			rowIndex = startRow;
		}
        //一般数据样式
        HSSFCellStyle cellStyle = workbook.createCellStyle();
        
        List<String> ls = new ArrayList<String>(); 
        for (Document doc : list) {  //放入数据
			ls.add(doc.getProduct());
			ls.add(doc.getTitle());
			ls.add(doc.getLang());
			ls.add(doc.getIfRelease());
			if(doc.getReleaseTime() == null) {
				ls.add("0000-00-00 00:00:00");
			}else {
				ls.add(doc.getReleaseTime().toString());
			}
			ls.add(doc.getFirstMenu());
			ls.add(doc.getSecondMenu());
			ls.add(doc.getThirdMenu());
			ls.add(doc.getFourthMenu());
			ls.add(doc.getFiveMenu());
			ls.add(doc.getWebUrl());
			ls.add(doc.getGithubUrl());
			ls.add(doc.getSource());
			
			HSSFRow rows = sheet.createRow(rowIndex);
	        for (int i = 0; i < ls.size(); i++) {
	            rows.setHeightInPoints(20f);
            	HSSFCell cell = rows.createCell(i);//参数为：列数index
                cell.setCellValue(ls.get(i));
                cell.setCellStyle(cellStyle);
	        }
	        ls.clear();
	        rowIndex++;
        }
        OutputStream outputStream = null;
        try {
//        	File file = new File("/data/gitUrlIntl.xls");
            File file = new File("/data/gitUrlCh.xls"); 
            outputStream = new FileOutputStream(file);
            workbook.write(outputStream);
            flag = true;
        } catch (IOException e) {
            System.out.println(e.getMessage());
            flag = false;
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
                flag = true;
            } catch (Exception e) {
                System.out.println(e.getMessage());
                flag = false;
            }
        }
		return flag;
	}

	public static void main(String[] args) throws IOException, SQLException {
		search();
		System.out.println("done!");
	}
}

