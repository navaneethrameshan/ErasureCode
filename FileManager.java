import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;


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

		//return byteChunk ;

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
			//if(i<noPartitions-1)
			//byteChunk[i]=new byte[partitionSize];
			//else
			byteChunk[i]=new byte[partitionSize + fileSize%partitionSize];
		}

		for(int i=0; i<noPartitions; i++)
		{
			//System.out.println(i + " " + byteChunk[i].length );

		}

		try 
		{
			fInputStream = new FileInputStream(inputFile);

			while (fileSize > 0) 
			{
				//System.out.println("startloop " + index);
				if (fileSize < 2*partitionSize) 
				{
					readLength = fileSize;
				}
				//System.out.println(" readlength " + readLength + " Ind: " +index + "bytesize "+byteChunk[index].length);
				// byteChunk = new byte[readLength];
				read = fInputStream.read(byteChunk[index], 0, readLength);
				// System.out.println("checkpoint ind " + index + " ReadSize " + read);
				fileSize -= read;
				//  offset+=read;
				// assert(read==byteChunk[index].length);
				partitionNumber++;
				newName = inputFile.getName() + ".part" + Integer.toString(partitionNumber - 1);
				partition = new FileOutputStream(new File(Parameters.outPutFilePath+newName));
				partition.write(byteChunk[index], 0, read);
				//System.out.println("Split1 " + read);
				partition.flush();
				partition.close();
				// byteChunk = null;
				partition = null;
				//System.out.println("endloop " + index);
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



	public void reconstruct()
	{
		//System.out.println("Construct");
		File f= new File(Parameters.outPutFilePathTest);
		File[] files=f.listFiles();

		System.out.println("FILES: " +files.length);
		//	for(int i=0; i< 4;i++)
		//  files[i]  = new File("music.mp3.part"+i);

		File ofile = new File("output.mp4");
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
				// assert(bytesRead == fileBytes.length);
				//assert(bytesRead == (int) file.length());
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
		System.out.println("XorFile bytechunkLength; " + lastPartitionSize);
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
			//System.out.println("XorFile xorFinal; " + xor.length);
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

			//data=new byte[(int)file.length()];

			fos.write(data);
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
		//sort(combination);
		String path="Node"+node+"/partition.";
		for(int i=0;i< combination.length; i++)
		{
			//System.out.print("aaaaa"+ combination[i]);
			path+=combination[i];
		}
		return path;
	}

	public static void main(String[] args) 
	{
		String fileName=Parameters.inputFilePath;
		File inFile= new File(fileName); 
		FileManager instance=new FileManager(); 
		
		//Added///
		Regeneration regenerate = new Regeneration();
		//Added///
		
		byte[][] k=instance.split1(inFile, Parameters.TotalPartitionNo);

		String baseDirectoryName="Node", drName;

		for(int i=0; i<Parameters.TotalNodeNo; i++)
		{
			drName=baseDirectoryName+i;
			if((new File(drName)).exists())
				(new File(drName)).delete();

			(new File(drName)).mkdir();


			instance.xorWriteToFile(instance.byteChunk, Parameters.config[i][0] , drName );

			instance.xorWriteToFile(instance.byteChunk, Parameters.config[i][1] , drName );
			//store the file
		}

		//Added///
	//	regenerate.input();
		//Added///
		
		

		/*
		//instance.reconstruct();
		int[] i={0,1};
		String path=instance.filePath(4, i);
		System.out.println("Path1 " + path);
		byte[] p1=instance.readFileFrom(path);
		System.out.println("P1 " + p1.length);


		int[] j={0,1,3};
		path=instance.filePath(2, j);
		System.out.println("Path2 " + path);
		byte[] p2=instance.readFileFrom(path);
		System.out.println("P2 " + p2.length);


		byte[] p3= instance.xor2Files( p1 , p2 );
		System.out.println("P3 " + p3.length);
		instance.writeFileTo( p3, Parameters.outPutFilePathTest + "/partition.3");

		readFileFrom(String path)
		writeFileTo(byte[] data, String path)
		xor2Files( byte[] chunk1 , byte[] chunk2 )
		sort(int[] arr)
		filePath(int node, int[] combination)
	// */
	}

}
