import java.util.HashMap;


public class BasisVector {

	static final int TOTALNODES = 5;
	static final int PARTSPERNODE = 2;
	static final int VECTORSIZE = 4;

	public static int[][][] list = {
			{{1,0,0,0},{0,1,1,0}},
			{{0,1,0,0},{0,0,1,1}},
			{{0,0,1,0},{1,1,0,1}},
			{{0,0,0,1},{1,0,1,0}},
			{{1,1,0,0},{0,1,0,1}}
	};

	public BasisVector(){

	/*	objects = new HashMap<Integer, Integer[][]>();
		list = new int [TOTALNODES][][];

		for (int i =0;i<TOTALNODES;i++){
			list[i]= new int[PARTSPERNODE][];
			for(int j=0; j<PARTSPERNODE; j++){
				list[i][j] = new int[VECTORSIZE];
			}
		}*/

	}
	

}
