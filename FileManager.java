import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.*;


public class FileManager {


	private byte[][] byteChunk;
	private int ordinaryPartitionSize;
	private int lastPartitionSize;

	public void split(File inputFile, int noPartitions)
	{

		if(inputFile.length()>Integer.MAX_VALUE)
			return;

		FileInputStream fInputStream;
		FileOutputStream partition;
		String newName;
		int fileSize = (int) inputFile.length();
		int partitionSize  =  fileSize/noPartitions  ;
		int partitionNumber = 0, read = 0, readLength = partitionSize;

		byte[] byteChunk;

		try 
		{
			fInputStream = new FileInputStream(inputFile);
			int index=0;
			while (fileSize > 0) 
			{
				if (fileSize <= 2*partitionSize) 
				{
					readLength = fileSize;
				}
				byteChunk = new byte[readLength];
				read = fInputStream.read(byteChunk, 0, readLength);
				//System.out.println("checkpoint ind " + index + " ReadSize " + read);
				fileSize -= read;
				assert(read==byteChunk.length);
				partitionNumber++;
				newName = inputFile.getName() + ".part" + Integer.toString(partitionNumber - 1);
				partition = new FileOutputStream(new File(newName));
				partition.write(byteChunk);
				partition.flush();
				partition.close();
				byteChunk = null;
				partition = null;
				index++;
			}

			fInputStream.close();
			fInputStream = null;

		}catch(Exception e)
		{
			e.printStackTrace();
		}

	}


	public byte[][] split1(File inputFile, int noPartitions)
	{

		if(inputFile.length()>Integer.MAX_VALUE)
			return null;

		FileInputStream fInputStream;
		FileOutputStream partition;
		String newName;
		int fileSize = (int) inputFile.length();
		int partitionSize  =  fileSize/noPartitions  ;
		ordinaryPartitionSize=partitionSize;
		lastPartitionSize=partitionSize+fileSize%partitionSize;
		int partitionNumber = 0, read = 0, readLength = partitionSize;
		int index=0;

		byteChunk=new byte[noPartitions][];


		for(int i=0; i<noPartitions; i++)
		{
			byteChunk[i]=new byte[partitionSize + fileSize%partitionSize];
		}

		try 
		{
			fInputStream = new FileInputStream(inputFile);

			while (fileSize > 0) 
			{
				if (fileSize < 2*partitionSize) 
				{
					readLength = fileSize;
				}
				read = fInputStream.read(byteChunk[index], 0, readLength);
				fileSize -= read;
				partitionNumber++;
				newName = inputFile.getName() + ".part" + Integer.toString(partitionNumber - 1);
				partition = new FileOutputStream(new File(Parameters.outPutFilePath+newName));
				partition.write(byteChunk[index], 0, read);
				partition.flush();
				partition.close();
				partition = null;
				index++;


			}

			fInputStream.close();
			fInputStream = null;

		}catch(Exception e)
		{
			e.printStackTrace();
		}

		return byteChunk ;

	}


	public void reconstruct(String path)
	{
		File f= new File(path);
		File[] files=f.listFiles();

		System.out.println("FILES: " +files.length);

		File ofile = new File(path+ "output.mp4");
		FileOutputStream fos;
		FileInputStream fis;
		byte[] fileBytes;
		int bytesRead = 0;
		try {
			fos = new FileOutputStream(ofile,true);             
			for (File file : files) 
			{
				fis = new FileInputStream(file);
				fileBytes = new byte[(int) file.length()];
				bytesRead = fis.read(fileBytes, 0,(int) file.length());
				System.out.println("Reconstruct Read " + file.length());
				fos.write(fileBytes);
				fos.flush();
				fileBytes = null;
				fis.close();
				fis = null;
			}
			fos.close();
			fos = null;
		}catch(Exception e)
		{}

	}


	void xorWriteToFile(byte[][] data, int[] combination , String desDrName )
	{

		for(int i=0;i<combination.length;i++)
			combination[i]--;

		if(combination.length<1)
		{
			System.out.println("Error Combination wrong;");
			return;
		}
		String comb="", path="";

		//put comb in asc order
		for(int i:combination)
		{

			comb+=Integer.toString(i);
		}

		path = desDrName + "/partition." + comb;
		byte[] xor = new byte[lastPartitionSize];	
		//System.out.println("XorFile bytechunkLength; " + lastPartitionSize);
		if(combination.length<=1)	
		{

			xor=data[combination[0]]; 
		}
		else
		{

			for(int i=0;i<data[0].length;i++)
			{
				xor[i]= (byte)(data[combination[0]][i]^data[combination[1]][i]);

				if(combination.length==3)
					xor[i]= (byte)(data[combination[2]][i]^xor[i]);			
			}
		}	
		try
		{
			File ofile = new File(path);
			FileOutputStream fos;

			fos = new FileOutputStream(ofile,true);             


			fos.write(xor);
			fos.flush();
			xor = null;


			fos.close();
			fos = null;

		}catch(Exception e)
		{}
	}


	byte[] readFileFrom(String path)
	{
		byte[] data;
		try
		{
			File file = new File(path);
			FileInputStream fis = new FileInputStream(file);
			data=new byte[(int)file.length()];

			fis.read(data, 0,(int) file.length());


			fis.close();
			fis = null;

			return data;

		}catch(Exception e)
		{}

		return null;
	}

	void writeFileTo(byte[] data, String path)
	{

		try
		{
			File file = new File(path);
			FileOutputStream fos = new FileOutputStream(file);


			fos.write(data);
			fos.flush();
			fos.close();
			fos = null;


		}catch(Exception e)
		{}


	}

	void writeFileTo(byte[] data, String path,  int size)
	{

		try
		{
			File file = new File(path);
			FileOutputStream fos = new FileOutputStream(file);


			fos.write(data, 0, size);
			fos.flush();
			fos.close();
			fos = null;


		}catch(Exception e)
		{}


	}

	byte[] xor2Files( byte[] chunk1 , byte[] chunk2 )
	{
		byte[] xor= new byte[ chunk1.length];
		for(int i=0; i < chunk1.length ;i++)
		{
			xor[i]= (byte)(chunk1[i]^chunk2[i]);		
		}
		return xor;
	}

	void sort(int[] arr)
	{
		int min=arr[0], ind=0, temp;
		for(int i=0;i<arr.length; i++)
		{
			for(int j=i;j<arr.length; j++)
			{
				if(arr[j]<min)
				{	
					min=arr[j];
					ind=j;
				}
			}
			temp=arr[i];
			arr[i]=min;
			arr[ind]=temp;
		}

	}

	String filePath(int node, int[] combination)
	{
		String path="Node"+node+"/partition.";
		for(int i=0;i< combination.length; i++)
		{
			path+=combination[i];
		}
		return path;
	}

	///////Added////////////////////////////////////////////////////

	String filePathFromPart(int node, int part)
	{
		String path="Node"+node+"/partition.";
		for(int k=0,l=0; k<BasisVector.VECTORSIZE; k++){
			if(BasisVector.list[node][part][k] == 1){
				path+= k;
			}
		}
		return path;
	}


	void fetchFromNodeToRegenerate(ArrayList<Integer[]> to_fetch, Integer failed_node){
		byte[] p2 = new byte[lastPartitionSize];
		int node=0, part=0;
		for(int i =0; i<to_fetch.size(); i++){
			for(int j=0;j<to_fetch.get(i).length; j++){
				node = (to_fetch.get(i)[j])/10;
				part = (to_fetch.get(i)[j])%10;
				String path = filePathFromPart(node, part);
				System.out.println("PATH:: " + path);
				byte[] p1= readFileFrom(path);
				p2 = xor2Files(p1, p2);
				//P2 has the final reconstructed object!!
			}
			//get path to write file
			String path1 = filePathFromPart(failed_node, i);
			System.out.println("Write to Path: "+ path1);
			(new File("Node"+failed_node)).mkdir();
			writeFileTo( p2, Parameters.outPutFilePathRegenerate + path1);

			for(int k=0; k<lastPartitionSize;k++){
				p2[k] =0;
			}
			System.out.println("-------");
		}

	}


	void fetchFromNodeToReconstruct(ArrayList<Integer[]> to_fetch){   // YET to actually reconstruct
		//get path to write file
		String path1 = "Reconstruct";
		if((new File(path1)).exists()){
			deleteDir(new File(path1)); 
		}
		(new File(path1)).mkdir();

		byte[] p2 = new byte[lastPartitionSize];
		int node=0, part=0;
		for(int i =0; i<to_fetch.size(); i++){
			for(int j=0;j<to_fetch.get(i).length; j++){
				node = (to_fetch.get(i)[j])/10;
				part = (to_fetch.get(i)[j])%10;
				String path = filePathFromPart(node, part);
				System.out.println("FETCH PATH:: " + path);
				byte[] p1= readFileFrom(path);
				p2 = xor2Files(p1, p2);
				//P2 has the final reconstructed object!!
			}
			int size=ordinaryPartitionSize;
			if(i==3)
				size=lastPartitionSize;
			else
				size=ordinaryPartitionSize;

			writeFileTo( p2, Parameters.outPutFilePathRegenerate + "Reconstruct/partition" + i,size);

			for(int k=0; k<lastPartitionSize;k++){
				p2[k] =0;
			}
			System.out.println("-------");
		}

		reconstruct(Parameters.outPutFilePathRegenerate + "Reconstruct/");

	}

	public boolean deleteDir(File dir) {
		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i=0; i<children.length; i++) {
				boolean success = deleteDir(new File(dir, children[i]));
				if (!success) {
					return false;
				}
			}
		}

		return dir.delete();
	}
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////


	public static void main(String[] args) 
	{
		String input;
		Scanner scanner = new Scanner(System.in);
		ArrayList<Integer[]> to_fetch;
		String fileName=Parameters.inputFilePath;
		File inFile= new File(fileName); 
		FileManager instance=new FileManager(); 

		//Added///
		Regeneration regenerate = new Regeneration();
		Reconstruct reconstruct = new Reconstruct();
		//Added///
		System.out.println("Splitting and storing the files in progress...");

		byte[][] k=instance.split1(inFile, Parameters.TotalPartitionNo);

		String baseDirectoryName="Node", drName;

		for(int i=0; i<Parameters.TotalNodeNo; i++)
		{
			drName=baseDirectoryName+i;
			if((new File(drName)).exists()){
				instance.deleteDir(new File(drName)); //added
			}
			(new File(drName)).mkdir();


			instance.xorWriteToFile(instance.byteChunk, Parameters.config[i][0] , drName );

			instance.xorWriteToFile(instance.byteChunk, Parameters.config[i][1] , drName );
			//store the file
		} 


		//Added///
		do{
			regenerate.clean();
			regenerate.input();
			if (!Regeneration.reconstruct){
				regenerate.printToFetchFinal();
				instance.fetchFromNodeToRegenerate(regenerate.toFetchFinal(), regenerate.failed_node);

			}

			else{
				reconstruct.clean();
				reconstruct.input();
				reconstruct.printToFetchFinal();
				instance.fetchFromNodeToReconstruct(reconstruct.toFetchFinal());
			}
			System.out.println("Do you want to perform more operations? ");
			input = scanner.next();
		}
		while(input.equals("y"));
		//Added///
	}

}
