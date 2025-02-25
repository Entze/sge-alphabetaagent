# SGE - AlphaBetaAgent

An agent for [sge](https://gitlab.com/StrategyGameEngine/Strategy-Game-Engine)
that utilises Alpha Beta Pruning to find the next move.

## Installation

The agent is bundled as a JAR. No separate installation is necessary.

## Usage

See also [sge](https://gitlab.com/StrategyGameEngine/Strategy-Game-Engine).

The following command starts a match of a game defined in `gameJar`. The SGE -
AlphaBetaAgent plays against an instance of itself.

```bash
sge match gameJar sge-alphabetaagent.jar AlphaBetaAgent AlphaBetaAgent
```

## Contributing

Pull requests are welcome. For major changes, please open an issue first
to discuss what you would like to change.

Please make sure to update tests as appropriate.

## License

[GPLv3](https://choosealicense.com/licenses/gpl-3.0/)
