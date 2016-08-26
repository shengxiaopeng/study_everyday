package com.jd.scf.component.excel;

import com.google.common.collect.Lists;
import com.jd.scf.component.constants.ComponentConst;
import com.jd.scf.utils.BeanUtils;
import com.jd.scf.utils.DateUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author zhaodisheng
 */
public  class ExcelExportUtils {

    public static void exportFile(String templateName,List dataList,HttpServletRequest request,HttpServletResponse response,Map<String,String> mod,int[] rowNum){

        try {
            response.setContentType("application/x-download");
            String filedisplay = getDisplayName(request, DateUtils.getCurrentDayAsString()+".xlsx");
            response.addHeader("Content-Disposition","attachment;filename="+filedisplay);
            exportFile(templateName,dataList,response.getOutputStream(),mod,rowNum);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void exportFile(String templateName,List dataList,HttpServletRequest request,HttpServletResponse response){
        exportFile(templateName,dataList,request,response,null,null);
    }

    public static byte[] exportFile(String templateName,List dataList){

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        exportFile(templateName,dataList,out,null,null);
        return out.toByteArray();
    }

    public static void exportFile(String templateName,List dataList,OutputStream outputStream,Map<String,String> mod,int[] rowNum){

        templateName = ExcelExportUtils.class.getResource("/tpl").getFile()+templateName+".xlsx";

        if(dataList.size()>0){
            Object obj = dataList.get(0);
            if(obj instanceof Map){
                exprotExcelForMap(templateName,dataList,outputStream,mod,rowNum);
            }else{
                exprotExcelForBean(templateName,dataList,outputStream,mod,rowNum);
            }
        }else{
            exprotExcelForMap(templateName,dataList,outputStream,mod,rowNum);
        }
    }

    private static String getDisplayName(HttpServletRequest request,String filename) throws Exception{

        String Agent = request.getHeader("User-Agent");
        if (null != Agent) {
            Agent = Agent.toLowerCase();
            if (Agent.indexOf("firefox") != -1) {
                filename = new String(filename.getBytes("utf-8"), "ISO8859-1");
            } else if (Agent.indexOf("msie") != -1) {
                filename = URLEncoder.encode(filename, "UTF-8");
            } else {
                filename = URLEncoder.encode(filename, "UTF-8");
            }
        }
        return filename;
    }

	private static void exprotExcelForBean(String filePath, List dataList,OutputStream outputStream,Map<String,String> mod,int[] rowNum)  {
        try {
            List<Map> value = BeanUtils.transBean2Map(dataList);
            exprotExcelForMap(filePath,value,outputStream,mod,rowNum);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void exprotExcelForMap(String filePath, List<Map> dataList,OutputStream outputStream,Map<String,String> mod,int[] rowNum)  {

        try {
            Workbook workBook =  getWorkbook(filePath);
            if(workBook == null) return;
            Sheet sheet = workBook.getSheetAt(0);
            int lastRowNum = sheet.getLastRowNum();
            Row row = sheet.getRow(lastRowNum);
            int cellNum = row.getLastCellNum();

            List<String> rowNames = Lists.newArrayList();
            List<CellStyle> styles = Lists.newArrayList();
            for(int i=0;i<cellNum;i++){
                Cell cell = row.getCell(i);
                rowNames.add(cell.getStringCellValue());
                styles.add(cell.getCellStyle());
            }

            for (int i=0;i<dataList.size();i++){
                Map<String,Object> rowData = dataList.get(i);
                rowData.put(ComponentConst.NUM,lastRowNum+i);
                createRow(sheet,rowData,rowNames,styles,lastRowNum+i);
            }

            if(rowNum != null && rowNum.length >0){
                for (int n = 0; n < rowNum.length; n++) {
                    Row custRow = sheet.getRow(rowNum[n]);
                    for (Iterator<Cell> iterator = custRow.iterator();iterator.hasNext();) {
                        Cell cell = iterator.next();
                        String cellVar = cell.getStringCellValue();
                        if (mod.containsKey(cellVar)) {
                            String value = mod.get(cellVar);
                            cell.setCellValue(value);
                        }
                    }
                }
            }

            workBook.write(outputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void createRow(Sheet sheet, Map<String,Object> rowData,List<String> mapping,List<CellStyle> styles,int startRowNum){

        Row row = sheet.createRow(startRowNum);

        for (int i =0;i<mapping.size();i++){
            String name  = mapping.get(i);
            Object obj = rowData.get(name);
            if(obj != null){
                Cell newCell = row.createCell(i);
                CellStyle style = styles.get(i);
                newCell.setCellStyle(style);
                if(obj instanceof Date){
                    newCell.setCellValue((Date)obj);
                }else if(obj instanceof BigDecimal){
                    double dd = ((BigDecimal)obj).doubleValue();
                    newCell.setCellValue(dd);
                }else{
                    newCell.setCellValue(obj.toString());
                }

            }
        }
    }

    private static Workbook getWorkbook(String path) throws IOException {

        FileInputStream fileInput = new FileInputStream(path);
        String postfix=path.substring(path.lastIndexOf("."));

        try{
            if(postfix.endsWith(ComponentConst.OFFICE_2003)){
                return new HSSFWorkbook(new POIFSFileSystem(fileInput));
            }else if(postfix.endsWith(ComponentConst.OFFICE_2007)){
                return WorkbookFactory.create(new File(path));
            }
        } catch (InvalidFormatException e) {
            throw new IOException(e);
        } finally {
            fileInput.close();
        }
        return null;
    }



    public static void main(String[] arge){
        try {
            Workbook workbook = getWorkbook("/E:/jdproject/newfcash/fcash-web/target/fcash-web/WEB-INF/classes/tpl/lendpay.xlsx");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static String[][] readExcel(ByteArrayInputStream in) {

        Workbook workBook = null;
        try {
            workBook = new XSSFWorkbook(in);
        } catch (IOException e) {
            e.printStackTrace();
            return new String[0][0];
        }
        Sheet sheet = workBook.getSheetAt(0);
        int lastRowNum = sheet.getLastRowNum();
        String[][] result = new String[lastRowNum+1][];
        for(int i = 0;i <= lastRowNum;i++){
            Row row = sheet.getRow(i);
            int cellNum = row.getLastCellNum();
            result[i] = new String[cellNum];
            for(int n=0;n<cellNum;n++){
                Cell cell = row.getCell(n);
                if(cell.getCellType() == Cell.CELL_TYPE_NUMERIC){
                    result[i][n] = cell.getNumericCellValue()+"";
                }
                if(cell.getCellType() == Cell.CELL_TYPE_STRING){
                    result[i][n] = cell.getStringCellValue();
                }
            }
        }
        return result;
    }
}