package xal.app.experimentautomator.core;

public class ResultsCalculator {

	String[] ProbeList;
	Double[][] CorrelationMatrix;
	Double[] data;
	int sizeOfProbeList;

	public ResultsCalculator(String[] probe, Double[][] matrix, int probeList) {
		ProbeList = probe;
		CorrelationMatrix = matrix;
		sizeOfProbeList = probeList;
		data = new Double[sizeOfProbeList];
	}

	public Double[] collectData() {
		for (int j = 0; j < sizeOfProbeList; j++) {
			// System.out.println(ProbeList[j]);

			if (ProbeList[j].equals("11"))
				data[j] = CorrelationMatrix[0][0];
			else if (ProbeList[j].equals("12") || ProbeList[j].equals("21"))
				data[j] = CorrelationMatrix[0][1];
			else if (ProbeList[j].equals("13") || ProbeList[j].equals("31"))
				data[j] = CorrelationMatrix[0][2];
			else if (ProbeList[j].equals("14") || ProbeList[j].equals("41"))
				data[j] = CorrelationMatrix[0][3];
			else if (ProbeList[j].equals("15") || ProbeList[j].equals("51"))
				data[j] = CorrelationMatrix[0][4];
			else if (ProbeList[j].equals("16") || ProbeList[j].equals("61"))
				data[j] = CorrelationMatrix[0][5];
			else if (ProbeList[j].equals("17") || ProbeList[j].equals("71"))
				data[j] = CorrelationMatrix[0][6];
			else if (ProbeList[j].equals("22"))
				data[j] = CorrelationMatrix[1][1];
			else if (ProbeList[j].equals("23") || ProbeList[j].equals("32"))
				data[j] = CorrelationMatrix[1][2];
			else if (ProbeList[j].equals("24") || ProbeList[j].equals("42"))
				data[j] = CorrelationMatrix[1][3];
			else if (ProbeList[j].equals("25") || ProbeList[j].equals("52"))
				data[j] = CorrelationMatrix[1][4];
			else if (ProbeList[j].equals("26") || ProbeList[j].equals("62"))
				data[j] = CorrelationMatrix[1][5];
			else if (ProbeList[j].equals("27") || ProbeList[j].equals("72"))
				data[j] = CorrelationMatrix[1][6];
			else if (ProbeList[j].equals("33"))
				data[j] = CorrelationMatrix[2][2];
			else if (ProbeList[j].equals("34") || ProbeList[j].equals("43"))
				data[j] = CorrelationMatrix[2][3];
			else if (ProbeList[j].equals("35") || ProbeList[j].equals("53"))
				data[j] = CorrelationMatrix[2][4];
			else if (ProbeList[j].equals("36") || ProbeList[j].equals("64"))
				data[j] = CorrelationMatrix[2][5];
			else if (ProbeList[j].equals("37") || ProbeList[j].equals("73"))
				data[j] = CorrelationMatrix[2][6];
			else if (ProbeList[j].equals("44"))
				data[j] = CorrelationMatrix[3][3];
			else if (ProbeList[j].equals("45") || ProbeList[j].equals("54"))
				data[j] = CorrelationMatrix[3][4];
			else if (ProbeList[j].equals("46") || ProbeList[j].equals("64"))
				data[j] = CorrelationMatrix[3][5];
			else if (ProbeList[j].equals("47") || ProbeList[j].equals("74"))
				data[j] = CorrelationMatrix[3][6];
			else if (ProbeList[j].equals("55"))
				data[j] = CorrelationMatrix[4][4];
			else if (ProbeList[j].equals("56") || ProbeList[j].equals("65"))
				data[j] = CorrelationMatrix[4][5];
			else if (ProbeList[j].equals("57") || ProbeList[j].equals("75"))
				data[j] = CorrelationMatrix[4][6];
			else if (ProbeList[j].equals("66"))
				data[j] = CorrelationMatrix[5][5];
			else if (ProbeList[j].equals("67") || ProbeList[j].equals("76"))
				data[j] = CorrelationMatrix[5][6];
			else if (ProbeList[j].equals("77"))
				data[j] = CorrelationMatrix[6][6];
			else {
				System.out
						.println("Error: Proble List entrty does not match anything in the correlation matrix");
				data[j] = 0.0;
			}
		}

		return data;
	}

}
