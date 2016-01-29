package ru.ispras.modis.NetBlox.parser.xmlParser;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class XMLElementProcessorFactory {
	private Map<Class<?>, Stack<XMLElementProcessor> > instanceList = new HashMap<Class<?>, Stack<XMLElementProcessor> >(); 
	
	public void push(XMLElementProcessor p) {
		Stack<XMLElementProcessor> stack;
		
		if (!instanceList.containsKey(p.getClass())) {
			stack = new Stack<XMLElementProcessor>();
			instanceList.put(p.getClass(), stack);
		} else {
			stack = instanceList.get(p.getClass());
		}
		
		stack.push(p);
	}
	
	public XMLElementProcessor pop(Class<?> c) {
		XMLElementProcessor result;
		
		if (instanceList.containsKey(c)) {
			result = instanceList.get(c).pop(); 
		} else {
			result = null;
		}
		
		return result;
	}
}
