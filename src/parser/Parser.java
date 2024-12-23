package parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import models.algebra.Constant;
import models.algebra.Expression;
import models.algebra.Symbol;
import models.algebra.Term;
import models.algebra.Type;
import models.algebra.Variable;
import models.dataConstraintModel.ChannelMember;
import models.dataConstraintModel.ResourcePath;
import models.dataConstraintModel.StateTransition;
import models.dataFlowModel.DataTransferModel;
import models.dataFlowModel.DataTransferChannel;
import parser.Parser.Token;
import parser.exceptions.ExpectedAssignment;
import parser.exceptions.ExpectedChannel;
import parser.exceptions.ExpectedChannelName;
import parser.exceptions.ExpectedEquals;
import parser.exceptions.ExpectedInOrOutOrRefKeyword;
import parser.exceptions.ExpectedLeftCurlyBracket;
import parser.exceptions.ExpectedRHSExpression;
import parser.exceptions.ExpectedRightBracket;
import parser.exceptions.ExpectedStateTransition;
import parser.exceptions.WrongLHSExpression;
import parser.exceptions.WrongRHSExpression;

public class Parser {		
	protected TokenStream stream;

	public static final String CHANNEL = "channel";
	public static final String INIT = "init";
	public static final String LEFT_CURLY_BRACKET = "{";
	public static final String RIGHT_CURLY_BRACKET = "}";
	public static final String LEFT_CURLY_BRACKET_REGX = "\\{";
	public static final String RIGHT_CURLY_BRACKET_REGX = "\\}";
	public static final String LEFT_BRACKET = "(";
	public static final String RIGHT_BRACKET = ")";
	public static final String LEFT_BRACKET_REGX = "\\(";
	public static final String RIGHT_BRACKET_REGX = "\\)";
	public static final String ADD = "+";
	public static final String MUL = "*";
	public static final String SUB = "-";
	public static final String DIV = "/";
	public static final String MOD = "%";
	public static final String MINUS = "-";
	public static final String EQ = "==";
	public static final String NEQ = "!=";
	public static final String GT = ">";
	public static final String LT = "<";
	public static final String GE = ">=";
	public static final String LE = "<=";
	public static final String AND = "&&";
	public static final String OR = "||";
	public static final String NEG = "!";
	public static final String ADD_REGX = "\\+";
	public static final String MUL_REGX = "\\*";
	public static final String SUB_REGX = "\\-";
	public static final String DIV_REGX = "/";
	public static final String IN = "in";
	public static final String OUT = "out";
	public static final String REF = "ref";
	public static final String OR_REGX = "\\|\\|";
	public static final String EQUALS = "=";
	public static final String ASSIGNMENT = "=";
	public static final String COMMA = ",";
	public static final String COLON = ":";
	public static final String DOUBLE_QUOT = "\"";

	public Parser(final TokenStream stream) {
		this.stream = stream;
	}

	public Parser(final BufferedReader reader) {		
		this.stream = new TokenStream();
		try {
			String line;
			while ((line = reader.readLine()) != null) {
				stream.addLine(line);
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public DataTransferModel doParse() 
			throws ExpectedRightBracket, ExpectedChannel, ExpectedChannelName, ExpectedLeftCurlyBracket, ExpectedInOrOutOrRefKeyword, ExpectedStateTransition, ExpectedEquals, ExpectedRHSExpression, WrongLHSExpression, WrongRHSExpression, ExpectedAssignment {
		return parseDataFlowModel();
	}

	public DataTransferModel parseDataFlowModel() 
			throws ExpectedRightBracket, ExpectedChannel, ExpectedChannelName, ExpectedLeftCurlyBracket, ExpectedInOrOutOrRefKeyword, ExpectedStateTransition, ExpectedEquals, ExpectedRHSExpression, WrongLHSExpression, WrongRHSExpression, ExpectedAssignment {
		DataTransferModel model = new DataTransferModel();
		DataTransferChannel channel;
		while ((channel = parseChannel(model)) != null) {
			if (channel.getInputChannelMembers().size() == 0) {
				model.addIOChannel(channel);
			} else {
				model.addChannel(channel);
			}
		}
		return model;
	}

	public DataTransferChannel parseChannel(DataTransferModel model) 
			throws 
			ExpectedLeftCurlyBracket, ExpectedRightBracket, ExpectedAssignment,
			ExpectedRHSExpression, WrongLHSExpression, WrongRHSExpression, 
			ExpectedChannel, ExpectedChannelName, ExpectedInOrOutOrRefKeyword, 
			ExpectedStateTransition, ExpectedEquals
	{
		if (!stream.hasNext()) return null;
		if (stream.checkNext().equals(RIGHT_CURLY_BRACKET)) return null;

		String channelOrInitKeyword = stream.next();
		if (!channelOrInitKeyword.equals(CHANNEL)) {
			if (!channelOrInitKeyword.equals(INIT))  throw new ExpectedChannel(stream.getLine());
			parseInit(model);
			channelOrInitKeyword = stream.next();
		}
		if (!stream.hasNext()) throw new ExpectedChannelName(stream.getLine());

		String channelName = stream.next();
		if (channelName.equals(LEFT_CURLY_BRACKET)) throw new ExpectedChannelName(stream.getLine());

		int fromLine = stream.getLine();
		DataTransferChannel channel = new DataTransferChannel(channelName);
		String leftBracket = stream.next();
		if (!leftBracket.equals(LEFT_CURLY_BRACKET)) throw new ExpectedLeftCurlyBracket(stream.getLine());

		String inOrOutOrRef = null;
		while (stream.hasNext() && !(inOrOutOrRef = stream.next()).equals(RIGHT_CURLY_BRACKET)) {
			ChannelMember channelMember = null;
			if (inOrOutOrRef.equals(IN)) {
				channelMember = parseChannelMember(model, inOrOutOrRef);
				if (channelMember != null) {
					channel.addChannelMemberAsInput(channelMember);
				}
			} else if (inOrOutOrRef.equals(OUT)) {
				channelMember = parseChannelMember(model, inOrOutOrRef);
				if (channelMember != null) {
					channel.addChannelMemberAsOutput(channelMember);
				}				
			} else if (inOrOutOrRef.equals(REF)) {
				channelMember = parseChannelMember(model, inOrOutOrRef);
				if (channelMember != null) {
					channel.addChannelMemberAsReference(channelMember);
				}				
			} else {
				throw new ExpectedInOrOutOrRefKeyword(stream.getLine());
			}
		}
		int toLine = stream.getLine();
		channel.setSourceText(stream.getSourceText(fromLine, toLine));
		return channel;
	}

	public void parseInit(DataTransferModel model) 
			throws 
			ExpectedLeftCurlyBracket, ExpectedAssignment, ExpectedRHSExpression, WrongRHSExpression, ExpectedRightBracket 
	{
		String leftBracket = stream.next();
		if (!leftBracket.equals(LEFT_CURLY_BRACKET)) throw new ExpectedLeftCurlyBracket(stream.getLine());
		String resourceName = null;		
		while (stream.hasNext() && !(resourceName = stream.next()).equals(RIGHT_CURLY_BRACKET)) {
			int fromLine = stream.getLine();
			ResourcePath resourcePath = model.getResourcePath(resourceName);
			if (resourcePath == null) {
				resourcePath = new ResourcePath(resourceName, 0);
				model.addResourcePath(resourcePath);
			}

			if (!stream.hasNext()) throw new ExpectedAssignment(stream.getLine());
			String colon = stream.next();
			if (!colon.equals(COLON)) throw new ExpectedAssignment(stream.getLine());
			if (!stream.hasNext()) throw new ExpectedAssignment(stream.getLine());
			String equals = stream.next();
			if (!equals.equals(ASSIGNMENT)) throw new ExpectedAssignment(stream.getLine());

			int toLine = stream.getLine();
			Expression rightTerm = null;
			if (!stream.hasNext()) throw new ExpectedRHSExpression(stream.getLine());		
			rightTerm = parseTerm(stream, model);		
			if (rightTerm == null) throw new WrongRHSExpression(stream.getLine());

			resourcePath.setInitialValue(rightTerm);
			resourcePath.setInitText(stream.getSourceText(fromLine, toLine));
		}
	}

	public ChannelMember parseChannelMember(DataTransferModel model, final String inOrOutOrRef) 
			throws 
			ExpectedRightBracket, ExpectedStateTransition, ExpectedEquals, 
			ExpectedRHSExpression, WrongLHSExpression, WrongRHSExpression
	{
		if (!stream.hasNext()) throw new ExpectedStateTransition(stream.getLine());
		Expression leftTerm = parseTerm(stream, model);
		if (leftTerm == null || !(leftTerm instanceof Term)) throw new WrongLHSExpression(stream.getLine());
		Expression rightTerm = null;

		if (!inOrOutOrRef.equals(REF)) {
			if (!stream.hasNext()) throw new ExpectedEquals(stream.getLine());
			String equals = stream.next();
			if (!equals.equals(EQUALS)) throw new ExpectedEquals(stream.getLine());

			if (!stream.hasNext()) throw new ExpectedRHSExpression(stream.getLine());		
			rightTerm = parseTerm(stream, model);		
			if (rightTerm == null) throw new WrongRHSExpression(stream.getLine());
		}

		String resourceName = ((Term) leftTerm).getSymbol().getName();
		ResourcePath resourcePath = model.getResourcePath(resourceName);
		if (resourcePath == null) {
			resourcePath = new ResourcePath(resourceName, 0);
			model.addResourcePath(resourcePath);
		}
		ChannelMember channelMember = new ChannelMember(resourcePath);
		StateTransition stateTransition = new StateTransition();
		stateTransition.setCurStateExpression(((Term) leftTerm).getChild(0));
		stateTransition.setMessageExpression(((Term) leftTerm).getChild(1));
		if (!inOrOutOrRef.equals(REF)) stateTransition.setNextStateExpression(rightTerm);
		channelMember.setStateTransition(stateTransition);
		// for type definition
		if (resourcePath.getResourceStateType() == null && ((Term) leftTerm).getChild(0) instanceof Variable) {
			Variable stateVar = (Variable) ((Term) leftTerm).getChild(0);
			if (stateVar.getType() != null) {
				resourcePath.setResourceStateType(stateVar.getType());
			}
		}
		if (((Term) leftTerm).getChild(1) instanceof Term) {
			Term messageTerm = (Term) ((Term) leftTerm).getChild(1);
			if (messageTerm.getSymbol().getSignature() == null && messageTerm.getChildren().size() > 0) {
				Type[] signature = new Type[messageTerm.getChildren().size() + 1];
				int i = 1;
				for (Expression e: messageTerm.getChildren()) {
					if (e instanceof Variable && ((Variable) e).getType() != null) {
						signature[i] = ((Variable) e).getType();
					}
					i++;
				}
				messageTerm.getSymbol().setSignature(signature);
			}
		}
		return channelMember;
	}

	public Expression parseTerm(TokenStream stream, DataTransferModel model) 
			throws ExpectedRightBracket
	{
		ArrayList<Expression> expressions = new ArrayList<>();
		ArrayList<Symbol> operators = new ArrayList<>();
		String operator = null;
		for (;;) {
			String leftBracketOrMinusOrNeg = stream.next();
			if (leftBracketOrMinusOrNeg.equals(LEFT_BRACKET)) {
				Expression exp = parseTerm(stream, model);
				String rightBracket = stream.next();
				if (!rightBracket.equals(RIGHT_BRACKET)) throw new ExpectedRightBracket(stream.getLine());
				expressions.add(exp);
			} else {
				Symbol minusOrNeg = null;
				String symbolName = null;
				if (leftBracketOrMinusOrNeg.equals(MINUS)) {
					minusOrNeg = DataTransferModel.minus;		// not sub
					symbolName = stream.next();
				} else if (leftBracketOrMinusOrNeg.equals(NEG)) {
					minusOrNeg = DataTransferModel.neg;
					symbolName = stream.next();
				} else {
					symbolName = leftBracketOrMinusOrNeg;
				}
				Expression exp = null;
				if (stream.checkNext() != null && stream.checkNext().equals(LEFT_BRACKET)) {
					// a function symbol
					Symbol symbol = model.getSymbol(symbolName);
					if (symbol == null) {
						symbol = new Symbol(symbolName);
						model.addSymbol(symbol);
					}
					Term term = new Term(symbol);
					int arity = 0;
					do {
						stream.next();		// LEFT_BRACKET or COMMA
						arity++;
						Expression subTerm = parseTerm(stream, model);
						term.addChild(subTerm, true);
						if (!stream.hasNext()) throw new ExpectedRightBracket(stream.getLine());
					} while (stream.checkNext().equals(COMMA));
					String rightBracket = stream.next();
					if (!rightBracket.equals(RIGHT_BRACKET)) throw new ExpectedRightBracket(stream.getLine());
					symbol.setArity(arity);
					exp = term;
				} else {
					// constant or variable
					try {
						Symbol symbol = model.getSymbol(symbolName);
						if (symbol != null && symbol.getArity() == 0) {
							exp = new Constant(symbol);
						} else {
							Double d = Double.parseDouble(symbolName);
							if (symbolName.contains(".")) {
								exp = new Constant(symbolName, DataTransferModel.typeDouble);
							} else {
								exp = new Constant(symbolName, DataTransferModel.typeInt);
							}
						}
					} catch (NumberFormatException e) {
						if (symbolName.startsWith("\"") && symbolName.endsWith("\"")) {
							// a string value
							exp = new Constant(symbolName, DataTransferModel.typeString);
						} else {
							// a variable
							if (stream.checkNext() != null && stream.checkNext().equals(COLON)) {
								// when a type is specified.
								stream.next();
								String typeName = stream.next();
								Type type = model.getType(typeName);
								if (type == null) {
									type = new Type(typeName, typeName);
								}
								exp = new Variable(symbolName, type);							
							} else {
								exp = new Variable(symbolName);
							}
						}
					}
				}
				if (minusOrNeg != null) {
					Term minusOrNegTerm = new Term(minusOrNeg);
					minusOrNegTerm.addChild(exp);
					expressions.add(minusOrNegTerm);
				} else {
					expressions.add(exp);
				}
			}
			operator = stream.checkNext();
			if (operator == null) {
				break;
			} else if (operator.equals(ADD)) {
				operators.add(DataTransferModel.add);
				stream.next();
			} else if (operator.equals(MUL)) {
				operators.add(DataTransferModel.mul);
				stream.next();
			} else if (operator.equals(SUB)) {
				operators.add(DataTransferModel.sub);	// not minus
				stream.next();
			} else if (operator.equals(DIV)) {
				operators.add(DataTransferModel.div);
				stream.next();
			} else if (operator.equals(MOD)) {
				operators.add(DataTransferModel.mod);
				stream.next();
			} else if (operator.equals(EQ)) {
				operators.add(DataTransferModel.eq);
				stream.next();
			} else if (operator.equals(NEQ)) {
				operators.add(DataTransferModel.neq);
				stream.next();
			} else if (operator.equals(GT)) {
				operators.add(DataTransferModel.gt);
				stream.next();
			} else if (operator.equals(LT)) {
				operators.add(DataTransferModel.lt);
				stream.next();
			} else if (operator.equals(GE)) {
				operators.add(DataTransferModel.ge);
				stream.next();
			} else if (operator.equals(LE)) {
				operators.add(DataTransferModel.le);
				stream.next();
			} else if (operator.equals(AND)) {
				operators.add(DataTransferModel.and);
				stream.next();
			} else if (operator.equals(OR)) {
				operators.add(DataTransferModel.or);
				stream.next();
			} else {
				break;
			}
		}
		if (expressions.size() == 1) {
			// no arithmetic operators
			return expressions.get(0);
		}
		ArrayList<Expression> monomials = new ArrayList<>();
		ArrayList<Symbol> addSubs = new ArrayList<>();
		Expression first = expressions.get(0);
		int i = 1;
		Term rootTerm = null;
		for (Symbol op: operators) {
			Expression second = expressions.get(i);
			if (op.getName().equals(MUL) || op.getName().equals(DIV) || op.getName().equals(MOD)) {
				// higher priority than add and sub
				Term term = new Term(op);
				term.addChild(first);
				term.addChild(second);
				first = term;
			} else if (op.getName().equals(EQ) || op.getName().equals(NEQ) || op.getName().equals(GT) || op.getName().equals(LT)
					|| op.getName().equals(GE) || op.getName().equals(LE) || op.getName().equals(AND) || op.getName().equals(OR)) {
				// lower priority than add and sub
				if (first != null) monomials.add(first);
				Expression firstMonomial = monomials.get(0);
				i = 1;
				for (Symbol op2: addSubs) {
					Expression secondMonomial = monomials.get(i);
					Term term = new Term(op2);
					term.addChild(firstMonomial);
					term.addChild(secondMonomial);
					firstMonomial = term;
					i++;
				}
				if (rootTerm == null) {
					rootTerm = new Term(op);
					rootTerm.addChild(firstMonomial);
				} else {
					rootTerm.addChild(firstMonomial);
					firstMonomial = rootTerm;
					rootTerm = new Term(op);
					rootTerm.addChild(firstMonomial);
				}
				monomials.clear();
				addSubs.clear();
			} else {
				// add or sub ==> new monomial
				monomials.add(first);
				addSubs.add(op);
				first = second;
			}
			i++;
		}
		if (first != null) monomials.add(first);
		Expression firstMonomial = monomials.get(0);
		i = 1;
		for (Symbol op: addSubs) {
			Expression secondMonomial = monomials.get(i);
			Term term = new Term(op);
			term.addChild(firstMonomial);
			term.addChild(secondMonomial);
			firstMonomial = term;
			i++;
		}
		if (rootTerm == null) {
			return firstMonomial;
		} else {
			rootTerm.addChild(firstMonomial);
			return rootTerm;
		}
	}

	/**--------------------------------------------------------------------------------
	 * [protected]
	/**--------------------------------------------------------------------------------
	 * checking the token has a token.
	 * 
	 * @param token
	 * @param specificTokenName
	 */	
	protected Boolean isMatchKeyword(final String token, final String specificTokenName) {
		if(token == null) return false;
		if(specificTokenName == null) return false;
		return token.equals(specificTokenName);
	}

	/**--------------------------------------------------------------------------------
	 * [inner class]
	 * "TokenStream" has a token what is read from description of "Architecture Language Model".
	 */
	public static class TokenStream {
		private ArrayList<ArrayList<Token>> tokens = new ArrayList<>();
		private ArrayList<String> lines = new ArrayList<>();
		private int line = 0;
		private int n = 0;

		public TokenStream() {
			line = 0;
			n = 0;
		}

		public void addLine(String line) {
			lines.add(line);
			line = line.trim();
			ArrayList<Token> tokenList = splitByDoubleQuotation(line);
			tokenList = splitBy(tokenList, ADD, ADD_REGX);
			tokenList = splitBy(tokenList, MUL, MUL_REGX);
			tokenList = splitBy(tokenList, SUB, SUB_REGX);
			tokenList = splitBy(tokenList, DIV, DIV_REGX);
			tokenList = splitBy(tokenList, MOD, MOD);
			tokenList = splitBy(tokenList, EQ, EQ);
			tokenList = splitBy(tokenList, NEQ,NEQ);
			tokenList = splitBy(tokenList, GE, GE);
			tokenList = splitBy(tokenList, LE, LE);
			tokenList = splitBy(tokenList, GT, GT);
			tokenList = splitBy(tokenList, LT, LT);
			tokenList = splitBy(tokenList, AND, AND);
			tokenList = splitBy(tokenList, OR, OR_REGX);
			tokenList = splitBy(tokenList, NEG, NEG);
			tokenList = splitBy(tokenList, COMMA, COMMA);
			tokenList = splitBy(tokenList, COLON, COLON);
			tokenList = splitBy(tokenList, LEFT_BRACKET, LEFT_BRACKET_REGX);
			tokenList = splitBy(tokenList, RIGHT_BRACKET, RIGHT_BRACKET_REGX);
			tokenList = splitBy(tokenList, EQUALS, EQUALS);
			tokenList = splitBy(tokenList, LEFT_CURLY_BRACKET, LEFT_CURLY_BRACKET_REGX);
			tokenList = splitBy(tokenList, RIGHT_CURLY_BRACKET, RIGHT_CURLY_BRACKET_REGX);
			tokens.add(tokenList);
		}

		private ArrayList<Token> splitBy(final List<Token> tokens, final String delimiter, final String delimiterRegx) {
			ArrayList<Token> newTokens = new ArrayList<>();
			for (Token token: tokens) {
				if (token.isAtomic()) {
					newTokens.add(token);
				} else {
					String[] splitTokens = token.split(delimiterRegx);
					boolean fFirstToken = true;
					for (String t: splitTokens) {
						if (!fFirstToken) {
							newTokens.add(new Token(delimiter, true));
						}
						if (t.length() > 0) {
							newTokens.add(new Token(t));
						}
						fFirstToken = false;
					}
					while (token.endsWith(delimiter)) {
						newTokens.add(new Token(delimiter, true));
						token = token.substring(0, token.length() - 1);
					}
				}
			}
			return newTokens;
		}
		
		private ArrayList<Token> splitByDoubleQuotation(String line) {
			ArrayList<Token> newTokens = new ArrayList<>();
			String[] tokens = line.split(DOUBLE_QUOT);
			boolean fFirstToken = true;
			for (int i = 0; i < tokens.length; i++) {
				String token = tokens[i];
				if (!fFirstToken) {
					newTokens.add(new Token(DOUBLE_QUOT, true));
				}
				if (!fFirstToken || token.length() > 0) {
					if (i % 2 == 0) {
						for (String t: token.split("[ \t]")) {
							newTokens.add(new Token(t));
						}
					} else {
						// string literal
						newTokens.add(new Token(token, true));
					}
				}
				fFirstToken = false;
			}
			if (line.endsWith(DOUBLE_QUOT)) {
				newTokens.add(new Token(DOUBLE_QUOT, true));
			}
			return newTokens;
		}

		public String next() {
			if (line >= tokens.size()) return null;
			while (n >= tokens.get(line).size()) {
				line++;
				n = 0;
				if (line >= tokens.size()) return null;
			}
			String token = tokens.get(line).get(n).getTokenStr();
			n++;
			return token;
		}

		public String checkNext() {
			if (line >= tokens.size()) return null;
			while (n >= tokens.get(line).size()) {
				line++;
				n = 0;
				if (line >= tokens.size()) return null;
			}
			return tokens.get(line).get(n).getTokenStr();
		}

		public boolean hasNext() {
			if (line >= tokens.size()) return false;
			while (n >= tokens.get(line).size()) {
				line++;
				n = 0;
				if (line >= tokens.size()) return false;
			}
			return true;
		}

		public int getLine() {
			return line;
		}

		public String getSourceText(int from, int to) {
			String text = "";
			for (int l = from; l <= to; l++) {
				text += lines.get(l) + "\n";
			}
			return text;
		}
	}
	
	public static class Token {
		String token;
		boolean isAtomic = false;
		
		public Token(String token) {
			this.token = token;
		}
	
		public Token(String token, boolean isAtomic) {
			this.token = token;
			this.isAtomic = isAtomic;
		}
		
		public String getTokenStr() {
			return token;
		}
		
		public boolean isAtomic() {
			return isAtomic;
		}
		
		public String[] split(String delimiterRegx) {
			return token.split(delimiterRegx);
		}
	
		public boolean endsWith(String delimiter) {
			return token.endsWith(delimiter);
		}
	
		public int length() {
			return token.length();
		}
	
		public Token substring(int beginIdx, int endIdx) {
			return new Token(token.substring(beginIdx, endIdx));
		}
	}
}
