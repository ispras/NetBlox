package ru.ispras.modis.NetBlox;

import java.util.Collection;
import java.util.Map;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;

import ru.ispras.modis.NetBlox.configuration.SystemConfiguration;
import ru.ispras.modis.NetBlox.scenario.ScenarioTask;

/**
 * This class controls all aspects of the application's execution
 */
public class Application implements IApplication {
	private static final String CONFIG_FILE_KEY = "-conf";


	public Object start(IApplicationContext context) throws Exception {
		Map<?, ?> contextArguments = context.getArguments();
		String[] args = (String[]) contextArguments.get("application.args");

		String pathToScenario = "";
		if (args.length < 1)	{
			System.out.println("The pass to scenario file is missing!\n");
			System.exit(1);
		}
		else if (args.length == 1)	{
			pathToScenario = args[0];
		}
		else	{
			for (int i=0 ; i<args.length ; i++)	{
				String key = args[i];
				String value = null;

				if (!key.startsWith("-"))	{	//For now we have only only 1 launch parameter that isn't preceded by a key: path to scenario.
					pathToScenario = key;
					continue;
				}

				int j = i+1;
				if (args.length >= j)	{	//XXX What if we have a null-value launch key that is followed by the path to scenario? Make a check!
					String candidate = args[j];
					if (!candidate.startsWith("-"))	{
						value = candidate;
						i++;
					}
				}

				processLaunchKey(key, value);
			}
		}

		Collection<ScenarioTask> scenario = getScenario(pathToScenario);

		if (scenario != null)	{
			CentralOperator operator = new CentralOperator(scenario);
			operator.executeScenario();
		}

		return IApplication.EXIT_OK;
	}

	private void processLaunchKey(String key, String value)	{
		if (key.equals(CONFIG_FILE_KEY))	{
			SystemConfiguration.initiate(value);
		}
	}


	public void stop() {
		// nothing to do
	}


	private static Collection<ScenarioTask> getScenario(String pathToScenario)	{
		Collection<ScenarioTask> scenario = ScenarioReader.read(pathToScenario);

		if (scenario == null)	{
			System.out.println("Could not start parsing the file: "+pathToScenario);
			return null;
		}
		else if (scenario.size() == 0)	{
			System.out.println("No tasks in the file: "+pathToScenario+"\n or the file is incorrect.");
			return null;
		}

		System.out.println("Scenario has been read from "+pathToScenario+"\nNumber of tasks: "+scenario.size());
		return scenario;
	}
}
