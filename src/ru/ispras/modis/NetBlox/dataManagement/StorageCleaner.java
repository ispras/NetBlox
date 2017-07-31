package ru.ispras.modis.NetBlox.dataManagement;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ru.ispras.modis.NetBlox.configuration.SystemConfiguration;
import ru.ispras.modis.NetBlox.dataStructures.internalMechs.AnalysedDataIdentifier;
import ru.ispras.modis.NetBlox.dataStructures.internalMechs.AnalysedDataIdentifier.Type;
import ru.ispras.modis.NetBlox.dataStructures.internalMechs.ExtendedMiningParameters;
import ru.ispras.modis.NetBlox.scenario.GraphMiningParametersSet;
import ru.ispras.modis.NetBlox.scenario.GraphParametersSet;
import ru.ispras.modis.NetBlox.scenario.MeasureParametersSet;
import ru.ispras.modis.NetBlox.scenario.ParametersSet;
import ru.ispras.modis.NetBlox.scenario.ValueFromRange;
import ru.ispras.modis.NetBlox.utils.Pair;

public class StorageCleaner extends StorageHandler {
	private static final String GRAPH_FILES_ROOT = SystemConfiguration.getInstance().getGraphFilesRoot();


	private static Map<ContentType, Boolean> allowDeleteContent = new HashMap<ContentType, Boolean>(3);	//XXX Replace by an array?

	public static void allowDeleteContent()	{
		allowDeleteContent.put(ContentType.GRAPH_EDGES, true);
		allowDeleteContent.put(ContentType.NODES_GROUPS, true);
		allowDeleteContent.put(ContentType.CHARACTERISTIC, true);
	}

	public static void deleteMined(GraphOnDriveHandler graphHandler, ExtendedMiningParameters extendedMiningParameters, ContentType contentType)	{
		if (!allowDeleteContent.get(contentType))	{
			return;
		}

		String storageDirectoryPathString = getPathStringToMinedDataDirectory(graphHandler, extendedMiningParameters);
		File[] contentToDelete = (new File(storageDirectoryPathString)).listFiles(new FilenameStartsWithFilter(contentType));
		if (contentToDelete != null)	{
			for (File toDelete : contentToDelete)	{
				toDelete.delete();
			}
		}
		allowDeleteContent.put(contentType, false);
	}


	public static void clear(GraphOnDriveHandler graphHandler, AnalysedDataIdentifier analysedDataIdentifier,
			MeasureParametersSet characteristicParameters)	{
		if (characteristicParameters != null)	{
			String storageFilePathString = getPathToMeasureStorageFile(graphHandler, analysedDataIdentifier, characteristicParameters);
			File file = new File(storageFilePathString);
			if (file.exists())	{	file.delete();	}

			String parentFolderPathstring = file.getParentFile().getAbsolutePath();
			eraseContentAndFolder(parentFolderPathstring);
		}
		else if (analysedDataIdentifier != null  &&  analysedDataIdentifier.type() == Type.MINED)	{
			ExtendedMiningParameters extendedMiningParameters = analysedDataIdentifier.getMiningParameters();
			String minedDataDirectoryPathstring = getPathStringToMinedDataDirectory(graphHandler, extendedMiningParameters);
			eraseContentAndFolder(minedDataDirectoryPathstring);
		}
		else	{	//Delete the graph - together with all results on it.
			eraseContentAndFolder(graphHandler.getAbsoluteGraphDirectoryPathString());
		}
	}

	public static void clearall(GraphOnDriveHandler graphHandler, AnalysedDataIdentifier analysedDataIdentifier,
			MeasureParametersSet characteristicParameters)	{
		GraphParametersSet graphParameters = graphHandler.getGraphParameters();
		String graphTypeDirectory = GRAPH_FILES_ROOT + SystemConfiguration.FILES_SEPARATOR +
				graphHandler.generateGraphTypeRelativeDirectory(graphParameters).toString();	//fixed for a graph description
		Set<String> graphContainingSubfoldersPaths = collectSubdirectories(graphParameters, graphTypeDirectory);

		if (graphContainingSubfoldersPaths.isEmpty())	{
			//There're just no graphs (and corresponding other results) in the storage that are described in scenario.
			return;
		}

		List<String> assembledPathstrings = new ArrayList<String>(graphContainingSubfoldersPaths.size());
		if (analysedDataIdentifier == null)	{	//If there's a statistic considered then it was computed over the basic graph.
			for (String graphSubdirectory : graphContainingSubfoldersPaths)	{
				assembledPathstrings.add(graphTypeDirectory + SystemConfiguration.FILES_SEPARATOR + graphSubdirectory);
			}
		}
		else if (analysedDataIdentifier.type() == Type.MINED)	{	// A statistic for graph mining results (if there's any).
			ExtendedMiningParameters extendedMiningParameters = analysedDataIdentifier.getMiningParameters();

			String absoluteExternalFileForMiningPathname = extendedMiningParameters.getAbsoluteExternalFilename();
			ValueFromRange<String> relativeExternalFileForMiningMention = extendedMiningParameters.getRelativeExternalFilename();
			GraphMiningParametersSet miningParameters = extendedMiningParameters.getMiningParameters();

			if (absoluteExternalFileForMiningPathname != null  &&  absoluteExternalFileForMiningPathname.equals(
					relativeExternalFileForMiningMention.getValue()))	{	//External path was provided and it was absolute.
				String minedRootString = absoluteExternalFileForMiningPathname + MINED_FOLDER_SUFFIX + PATH_SECTION_FOR_MINED_PREFIX +
						miningParameters.getAlgorithmName() + SystemConfiguration.FILES_SEPARATOR;
				Set<String> miningSubdirectoriesPaths = collectSubdirectories(miningParameters, minedRootString);

				assembledPathstrings = new ArrayList<String>(miningSubdirectoriesPaths.size());
				for (String minedSubdirectory : miningSubdirectoriesPaths)	{
					assembledPathstrings.add(minedRootString + minedSubdirectory);
				}
			}
			else	{
				String multipleExternalFilesForMiningPathSection = getMultipleExternalFilesForMiningPathSection(extendedMiningParameters);
				Iterator<String> graphSubdirectoriesIterator = graphContainingSubfoldersPaths.iterator();
				while (graphSubdirectoriesIterator.hasNext())	{
					String graphSubdirectoryPath = graphSubdirectoriesIterator.next();

					String externalPathSegmentToAppend = "";
					if (relativeExternalFileForMiningMention != null)	{
						externalPathSegmentToAppend = getRelativeExternalPathInterfix(relativeExternalFileForMiningMention.getValue() +
								MINED_FOLDER_SUFFIX, graphSubdirectoryPath, graphSubdirectoriesIterator);
						if (externalPathSegmentToAppend == null)	{
							continue;	//The graph subdirectory proved not to suit the currently analysed description from scenario.
						}
					}

					String minedRootString = graphTypeDirectory + SystemConfiguration.FILES_SEPARATOR +
							graphSubdirectoryPath + externalPathSegmentToAppend + PATH_SECTION_FOR_MINED_PREFIX +
							miningParameters.getAlgorithmName() + SystemConfiguration.FILES_SEPARATOR + multipleExternalFilesForMiningPathSection;
					Set<String> miningSubdirectoriesPaths = collectSubdirectories(miningParameters, minedRootString);

					for (String minedSubdirectory : miningSubdirectoriesPaths)	{
						assembledPathstrings.add(minedRootString + minedSubdirectory);
					}
				}
			}
		}
		else if (analysedDataIdentifier.type() == Type.EXTERNAL)	{	// A statistic for externally provided data (see <graphs/>; if any statistic at all).
			String providedPath = analysedDataIdentifier.getExternalFilepathAsInScenario();
			File file = new File(providedPath);
			if (file.isAbsolute())	{	//Absolute external path for quasi mined data. No considerations for original graph.
				assembledPathstrings = new ArrayList<String>(1);
				assembledPathstrings.add(providedPath);
			}
			else	{	//Relative external path. Located inside the graph directory? Not necessarily.
				Iterator<String> graphSubdirectoriesIterator = graphContainingSubfoldersPaths.iterator();
				while (graphSubdirectoriesIterator.hasNext())	{
					String graphSubdirectoryPath = graphSubdirectoriesIterator.next();
					String externalPathToAppend = getRelativeExternalPathInterfix(providedPath + STATS_FOLDER_SUFFIX,
							graphSubdirectoryPath, graphSubdirectoriesIterator);
					if (externalPathToAppend != null)	{
						assembledPathstrings.add(graphTypeDirectory + SystemConfiguration.FILES_SEPARATOR + graphSubdirectoryPath + externalPathToAppend);
					}
				}
			}
		}

		if (characteristicParameters != null)	{
			for (String alreadyAssembledPath : assembledPathstrings)	{
				String measureDirectoryPathstring = alreadyAssembledPath + PATH_SECTION_FOR_MEASURES_AND_STATS_PREFIX +
						characteristicParameters.getCharacteristicNameInScenario() + SystemConfiguration.FILES_SEPARATOR;
				Set<String> statsSubdirectoriesPaths = collectSubdirectories(characteristicParameters, measureDirectoryPathstring);
				for (String statsSubdirectoryPathstring : statsSubdirectoriesPaths)	{
					eraseContentAndFolder(measureDirectoryPathstring + statsSubdirectoryPathstring);
				}
			}
		}
		else	{
			for (String assembledPath : assembledPathstrings)	{
				eraseContentAndFolder(assembledPath);
			}
		}
	}


	private static Set<String> collectSubdirectories(ParametersSet parameters, String rootDirectoryPathname)	{
		Set<String> assembledSubdirectoriesPaths = new HashSet<String>();

		File rootDirectory = new File(rootDirectoryPathname);
		if (!(rootDirectory.exists() && rootDirectory.isDirectory()))	{
			return assembledSubdirectoriesPaths;
		}

		String[] initialSubdirectories = rootDirectory.list();	//The case of empty initialSubdirectories is dealt with by descendIntoSubfolders(...).
		descendIntoSubfolders("", initialSubdirectories, rootDirectoryPathname, assembledSubdirectoriesPaths);

		filterSubfoldersToSuitParameters(assembledSubdirectoriesPaths, parameters);

		return assembledSubdirectoriesPaths;
	}

	private static String getRelativeExternalPathInterfix(String externalPathname, String assembledPathname, Iterator<String> assembledPathnamesIterator)	{
		if (externalPathname.contains(".."))	{
			return externalPathname;
		}
		else	{	//<- means this relative path must have been considered while assembling subpaths.
			externalPathname = SystemConfiguration.FILES_SEPARATOR + externalPathname.replace("."+SystemConfiguration.FILES_SEPARATOR, "");
			if (externalPathname.startsWith(PATH_SECTION_FOR_MINED_PREFIX) || externalPathname.startsWith(PATH_SECTION_FOR_MEASURES_AND_STATS_PREFIX))	{
				//The SystemConfiguration.FILES_SEPARATOR was added^ at the beginning of pathname for this check, due to the structure of these prefixes.
				return externalPathname;
			}

			if (!assembledPathname.endsWith(externalPathname))	{
				//Doesn't suit current externally provided data specification.
				assembledPathnamesIterator.remove();
				return null;
			}
			return "";
		}
	}

	/**
	 * Filter out the paths that don't correspond to the description in scenario.
	 */
	private static void filterSubfoldersToSuitParameters(Set<String> candidateSubfoldersPaths, ParametersSet parameters)	{
		List<Pair<String, String>> parametersAsKeyValuePairs = parameters.getSpecifiedParametersAsPairsOfUniqueKeysAndValues();
		if (parametersAsKeyValuePairs == null)	{	//No parameters really for this plug-in => all collected subdirectories suit.
			return;
		}

		Iterator<String> candidatesIterator = candidateSubfoldersPaths.iterator();
		while (candidatesIterator.hasNext())	{
			String candidatePath = candidatesIterator.next();
			for (Pair<String, String> keyValuePair : parametersAsKeyValuePairs)	{
				//XXX Should this(v) "keyValuePair.getKey()+keyValuePair.getValue()" procedure be centralised in a single class?
				if (!candidatePath.contains(keyValuePair.getKey()+keyValuePair.getValue()))	{
					//XXX A mistake in case of parameter equals, say, 10 and 101, can occur. (like "par10" and "par101") A more thorough check is needed.
					//This candidate doesn't respond to the conditions in scenario.
					candidatesIterator.remove();
					break;
				}
			}
		}
	}

	private static void descendIntoSubfolders(String assembledPath, String[] subfoldersNames, String rootDirectoryPathname, Set<String> results)	{
		if (subfoldersNames == null  ||  subfoldersNames.length == 0)	{
			eraseFolderWithEmptyParents(new File(rootDirectoryPathname + assembledPath));
			return;
		}

		for (String subfolderName : subfoldersNames)	{
			String pathname = SystemConfiguration.FILES_SEPARATOR + subfolderName + SystemConfiguration.FILES_SEPARATOR;
			if (pathname.equals(PATH_SECTION_FOR_MINED_PREFIX) || pathname.equals(PATH_SECTION_FOR_MEASURES_AND_STATS_PREFIX))	{
				results.add(assembledPath);
				continue;
			}

			pathname = assembledPath + pathname;
			File folder = new File(rootDirectoryPathname + pathname);
			if (folder.isDirectory())	{
				descendIntoSubfolders(pathname, folder.list(), rootDirectoryPathname, results);
			}
			else	{
				results.add(assembledPath);
			}
		}
	}


	private static void eraseContentAndFolder(String pathstring)	{
		File folder = new File(pathstring);
		if (!folder.exists())	{
			return;
		}

		String[] contents = folder.list();
		int intactSubfoldersCounter = 0;
		for (String itemName : contents)	{
			File item = new File(pathstring + SystemConfiguration.FILES_SEPARATOR + itemName);
			if (item.isFile())	{
				item.delete();
			}
			else	{
				itemName = SystemConfiguration.FILES_SEPARATOR + itemName + SystemConfiguration.FILES_SEPARATOR;
				if (itemName.equals(PATH_SECTION_FOR_MINED_PREFIX) || itemName.equals(PATH_SECTION_FOR_MEASURES_AND_STATS_PREFIX) ||
						itemName.endsWith(MINED_FOLDER_SUFFIX) || itemName.endsWith(STATS_FOLDER_SUFFIX))	{
					eraseFolderWithChildren(item);
				}
				else	{
					intactSubfoldersCounter++;
				}
			}
		}

		if (intactSubfoldersCounter == 0)	{	//The folder is now empty.
			eraseFolderWithEmptyParents(folder);
		}
	}

	private static void eraseFolderWithEmptyParents(File folder)	{
		File parentFolder = folder.getParentFile();
		String parentFolderPathstring = folder.getParent();

		folder.delete();

		if (parentFolder.list().length == 0)	{	//The parent folder is empty.
			if (!parentFolderPathstring.equals(GRAPH_FILES_ROOT))	{
				eraseFolderWithEmptyParents(parentFolder);
			}
		}
	}

	private static void eraseFolderWithChildren(File folder)	{
		File[] contents = folder.listFiles();
		for (File item : contents)	{
			if (item.isDirectory())	{
				eraseFolderWithChildren(item);
			}
			else	{
				item.delete();
			}
		}
		folder.delete();
	}



	private static class FilenameStartsWithFilter implements FilenameFilter	{
		private String nameStartString;

		public FilenameStartsWithFilter(ContentType contentType)	{
			this.nameStartString = contentType.toString();
		}


		/**
		 * Tests if a specified file should be included in a file list.
		 * @param dir		- the directory in which the file was found.
		 * @param filename	- the name of the file.
		 */
		@Override
		public boolean accept(File dir, String filename) {
			return filename.startsWith(nameStartString);
		}
	}
}
