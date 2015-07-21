import net.sourceforge.jswarm_pso.Particle;
import net.sourceforge.jswarm_pso.ParticleUpdate;
import net.sourceforge.jswarm_pso.Swarm;

public class HmmRepulsiveUpdate extends ParticleUpdate 
{
	/** Random vector for local update */
	double rLocal;
	/** Random vector for global update */
	double rGlobal;
	/** Random vector for random otherParticle update */
	double rOther;
	
	/**
	 * Constructor 
	 * @param particle : Sample of particles that will be updated later
	 */
	public HmmRepulsiveUpdate(Particle particle) 
	{
		super(particle);
	}

	/** 
	 * This method is called at the begining of each iteration
	 * Initialize random vectors use for local and global updates (rlocal[] and rother[])
	 */
	@Override
	public void begin(Swarm swarm) 
	{
		randomizePullWeights();
	}
	
	public void randomizePullWeights()
	{
		rLocal = Math.random();
		rGlobal = Math.random();
		rOther = Math.random();
	}

	/**
	 * Update particle's position and velocity using repulsive algorithm
	 */
	@Override
	public void update(Swarm swarm, Particle particle) 
	{
		double position[] = particle.getPosition();
		double velocity[] = particle.getVelocity();
		double globalBestPosition[] = swarm.getBestPosition();
		double particleBestPosition[] = particle.getBestPosition();
		
		HmmSwarmRepulsive hmmSwarmRepulsive = (HmmSwarmRepulsive) swarm;
		
		// Randomly select other particle
		int randOtherParticle = (int) (Math.random() * swarm.size());
		double otherParticleBestPosition[] = swarm.getParticle(randOtherParticle).getBestPosition();
		
		// Update velocity and position
		for (int i = 0; i < position.length; i++) 
		{
			// Update position
			position[i] = position[i] + velocity[i];

			// Update velocity
			velocity[i] = hmmSwarmRepulsive.getInertia() * velocity[i] // Inertia
						  + rLocal * swarm.getParticleIncrement() * (particleBestPosition[i] - position[i]) // Local best
						  + rGlobal * swarm.getGlobalIncrement() * (globalBestPosition[i] - position[i]) // Global best					
						  + rOther * hmmSwarmRepulsive.getOtherParticleIncrement() * (otherParticleBestPosition[i] - position[i]); // other Particle Best Position
		}
		
		randomizePullWeights();
	}
}
