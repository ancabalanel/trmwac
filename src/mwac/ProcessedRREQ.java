package mwac;

class ProcessedRREQ{
	int source;
	int requestId;
	
	ProcessedRREQ(int source, int requestId) {
		super();
		this.source = source;
		this.requestId = requestId;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ProcessedRREQ other = (ProcessedRREQ) obj;
		if (requestId != other.requestId)
			return false;
		if (source != other.source)
			return false;
		return true;
	}


}