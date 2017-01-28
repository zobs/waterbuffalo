package BuffaloV0;
import battlecode.common.*;

/**
 * Created by Zach on 1/27/2017.
 */
public class CombatStrategy {

    /**
     * shotWillHit -OVERLOADED-
     *
     * @param loc (MapLocation) Location of rc
     * @param targetLoc (MapLocation) location to shoot at
     * @return (boolean) whether or not a shot you shoot at an enemy will actually hit an enemy
     * 					-- in particular, makes sure there aren't any trees in the way
     *
     * @throws GameActionException
     */

    public static boolean shotWillHit(MapLocation loc, MapLocation targetLoc) throws GameActionException{
        System.out.println("Before " + Clock.getBytecodeNum());
        MapLocation bullet_path_midpoint = loc.add(loc.directionTo(targetLoc), loc.distanceTo(targetLoc) / 2f);

        RobotInfo[] friendly_robots_near_target = RobotPlayer.rc.senseNearbyRobots(bullet_path_midpoint,loc.distanceTo(targetLoc) / 2f, RobotPlayer.FRIEND);
        TreeInfo[] friendly_trees_near_target = RobotPlayer.rc.senseNearbyTrees(bullet_path_midpoint,loc.distanceTo(targetLoc) / 2f, RobotPlayer.FRIEND);
        TreeInfo[] neutral_trees_near_target = RobotPlayer.rc.senseNearbyTrees(bullet_path_midpoint,loc.distanceTo(targetLoc) / 2f, RobotPlayer.NEUTRAL);
        TreeInfo[] enemy_trees_near_target = RobotPlayer.rc.senseNearbyTrees(bullet_path_midpoint,loc.distanceTo(targetLoc) / 2f, RobotPlayer.ENEMY);

        for (RobotInfo robot : friendly_robots_near_target){
            if (RobotPlayer.circleIntersectsLine(robot.getLocation(), robot.getType().bodyRadius, loc, targetLoc)){
                System.out.println("After " + Clock.getBytecodeNum());
                return false;
            }
        }

        for (TreeInfo tree : friendly_trees_near_target){
            if (RobotPlayer.circleIntersectsLine(tree.getLocation(), tree.radius, loc, targetLoc)){
                System.out.println("After " + Clock.getBytecodeNum());
                return false;
            }
        }

        for (TreeInfo tree : neutral_trees_near_target){
            if (RobotPlayer.circleIntersectsLine(tree.getLocation(), tree.radius, loc, targetLoc)){
                System.out.println("After " + Clock.getBytecodeNum());
                return false;
            }
        }

        for (TreeInfo tree : enemy_trees_near_target){
            if (RobotPlayer.circleIntersectsLine(tree.getLocation(), tree.radius, loc, targetLoc)){
                System.out.println("After " + Clock.getBytecodeNum());
                return false;
            }
        }

        System.out.println("After " + Clock.getBytecodeNum());
        return true;
    }

    /**
     * shotWillHit -OVERLOADED-
     *
     * @param loc (MapLocation) Location of rc
     * @param robotTarget (RobotInfo) robot target to shoot at
     * @return (boolean) acts exactly like previous iteration of function
     *
     * @throws GameActionException
     */

    public static boolean shotWillHit(MapLocation loc, RobotInfo robotTarget) throws GameActionException {
        return shotWillHit(loc, robotTarget.getLocation());
    }


    /**
     * locateTarget
     *
     * @return (MapLocation) location on map to move to if not attacking shit.
     * @throws GameActionException
     */

    public static MapLocation locateTarget(){
        RobotController rc = RobotPlayer.rc;
        MapLocation[] potentialTargets = RobotPlayer.last_sighting_location;

        //use secondary targets and find which type is the one we want to target
        int priorityType = 0;
        for(int i = 1; i < potentialTargets.length; i++) {
            if(!potentialTargets[i].equals(RobotPlayer.INVALID_LOCATION)
                    && rc.canMove(potentialTargets[i])
                    && RobotPlayer.getPriority(rc.getType(), RobotPlayer.intToType(i)) < RobotPlayer.getPriority(rc.getType(), RobotPlayer.intToType(priorityType))) {
                priorityType = i;
            }
        }
        return potentialTargets[priorityType];
    }

    /**
     * getShootingTarget
     *
     * @return (RobotInfo) enemy robot that is most optimal target.
     *      -- Prioritizes type, then distance, then health
     * @throws GameActionException
     */

    public static RobotInfo getShootingTarget() throws GameActionException{
        try {
            for (int i = 0; i < RobotPlayer.enemies.length; i++) {
                for (int j = 0; j < RobotPlayer.enemies[i].length; j++) {
                    if (shotWillHit(RobotPlayer.rc.getLocation(), RobotPlayer.enemies[i][j].getLocation())) {
                        return RobotPlayer.enemies[i][j];
                    }
                }
            }
        } catch(Exception e) {
            System.out.println("getShootingTarget() error");
            e.printStackTrace();
        }
        return null;
    }

    /**
     * getBattleStance
     *
     * @param target (RobotInfo) robot you wish to target
     * @return (MapLocation) location to move to for optimal unit spacing
     * @throws GameActionException
     */

    public static MapLocation getBattleStance(RobotInfo target) throws GameActionException{
        try {
            final int SPRING_CONSTANT = 1;
            float[] force = {0f, 0f}; //[x, y]
            float optimalDistance;
            float displacement;
            float distance;
            MapLocation ours = RobotPlayer.rc.getLocation();
            Direction dir;

            for (RobotInfo robot : RobotPlayer.robots) {
                if (shotWillHit(RobotPlayer.rc.getLocation(), robot.getLocation())) {
                    optimalDistance = RobotPlayer.getOptimalDist(RobotPlayer.rc.getType(), robot.getType());
                    distance = ours.distanceTo(robot.getLocation());
                    displacement = distance - optimalDistance;
                    dir = ours.directionTo(robot.getLocation());
                    if(displacement < 0 || robot.equals(target)) {
                        force[0] += SPRING_CONSTANT * displacement * Math.cos(dir.radians);
                        force[1] += SPRING_CONSTANT * displacement * Math.sin(dir.radians);
                    }
                }
            }
            return new MapLocation(ours.x + force[0], ours.y + force[1]);
        } catch(Exception e){
            System.out.println("getBattleStance() error");
            e.printStackTrace();;
        }
        return null;
    }
}
