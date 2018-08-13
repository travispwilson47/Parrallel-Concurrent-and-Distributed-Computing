package edu.coursera.parallel;

import java.util.List;
import java.security.KeyStore.Entry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A simple wrapper class for various analytics methods.
 */
public final class StudentAnalytics {
    /**
     * Sequentially computes the average age of all actively enrolled students
     * using loops.
     *
     * @param studentArray Student data for the class.
     * @return Average age of enrolled students
     */
    public double averageAgeOfEnrolledStudentsImperative(
            final Student[] studentArray) {
        List<Student> activeStudents = new ArrayList<Student>();

        for (Student s : studentArray) {
            if (s.checkIsCurrent()) {
                activeStudents.add(s);
            }
        }

        double ageSum = 0.0;
        for (Student s : activeStudents) {
            ageSum += s.getAge();
        }

        return ageSum / (double) activeStudents.size();
    }

    /**
     * TODO compute the average age of all actively enrolled students using
     * parallel streams. This should mirror the functionality of
     * averageAgeOfEnrolledStudentsImperative. This method should not use any
     * loops.
     *
     * @param studentArray Student data for the class.
     * @return Average age of enrolled students
     */
    public double averageAgeOfEnrolledStudentsParallelStream(
            final Student[] studentArray) {
    	
    	return Stream.of(studentArray)
    			.parallel()
    			.filter(a -> a.checkIsCurrent())
    			.mapToDouble(a -> a.getAge())
    			.average()
    			.getAsDouble();
    }

    /**
     * Sequentially computes the most common first name out of all students that
     * are no longer active in the class using loops.
     *
     * @param studentArray Student data for the class.
     * @return Most common first name of inactive students
     */
    public String mostCommonFirstNameOfInactiveStudentsImperative(
            final Student[] studentArray) {
        List<Student> inactiveStudents = new ArrayList<Student>();

        for (Student s : studentArray) {
            if (!s.checkIsCurrent()) {
                inactiveStudents.add(s);
            }
        }

        Map<String, Integer> nameCounts = new HashMap<String, Integer>();

        for (Student s : inactiveStudents) {
            if (nameCounts.containsKey(s.getFirstName())) {
                nameCounts.put(s.getFirstName(),
                        new Integer(nameCounts.get(s.getFirstName()) + 1));
            } else {
                nameCounts.put(s.getFirstName(), 1);
            }
        }

        String mostCommon = null;
        int mostCommonCount = -1;
        for (Map.Entry<String, Integer> entry : nameCounts.entrySet()) {
            if (mostCommon == null || entry.getValue() > mostCommonCount) {
                mostCommon = entry.getKey();
                mostCommonCount = entry.getValue();
            }
        }

        return mostCommon;
    }

    /**
     * TODO compute the most common first name out of all students that are no
     * longer active in the class using parallel streams. This should mirror the
     * functionality of mostCommonFirstNameOfInactiveStudentsImperative. This
     * method should not use any loops.
     *
     * @param studentArray Student data for the class.
     * @return Most common first name of inactive students
     */
    public String mostCommonFirstNameOfInactiveStudentsParallelStream(
            final Student[] studentArray) {
        ConcurrentMap<String, Integer> nameCounts = new ConcurrentHashMap<String, Integer>();
        
        Stream.of(studentArray)
        	.parallel()
        	.filter(a -> !a.checkIsCurrent())
        	.forEach( (s) -> {
        		checkMap(nameCounts, s);
        	});
        
        //factoring out the code is faster here because we have to run it with the number of unique names
        // also it has to be computed sequentially and cannot be acertained in parallel see below
        
        CommonStruct struct = new CommonStruct(null, -1);
        
        //Parallel slows down here by adding overhead; we must have sychronized methods to prevent data razes
        //but we end up just computing it sequentially 
        nameCounts.entrySet().stream()
        	.forEach((entry) -> {
        		if (struct.mostCommon == null || entry.getValue() > struct.mostCommonCount) {
                    struct.mostCommon = entry.getKey();
                    struct.mostCommonCount = entry.getValue();
                }
        	});
        		
    	
    	return struct.mostCommon;
    }
    
    public static synchronized void checkMap(ConcurrentMap<String, Integer> nameCounts, Student s) {
    	if (nameCounts.containsKey(s.getFirstName())) {
            nameCounts.put(s.getFirstName(),
                    new Integer(nameCounts.get(s.getFirstName()) + 1));
        } else {
            nameCounts.put(s.getFirstName(), 1);
        }
    }
    
    public static class CommonStruct{
    	String mostCommon;
    	int mostCommonCount;
    	public CommonStruct(String mostCommon, int mostCommonCount) {
    		this.mostCommon = mostCommon;
    		this.mostCommonCount = mostCommonCount;
    	}
    }
    /* Deprecated because this form has performance issues with hashmaps
    public static class NameDataStruct {
    	private ConcurrentMap<String, Integer> nameFreq = new ConcurrentHashMap<String, Integer>();
    	private String mostCommonName = null;
    	
    	public String mostCommon() { return mostCommonName;}
    	
    	
    	public void accept(String name) {
    		if (nameFreq.containsKey(name)) {
    			nameFreq.put(name, nameFreq.get(name) + 1);
    		} else {
    			nameFreq.put(name, 1);
    		}
    		
    		if (mostCommonName == null) {
				mostCommonName = name;
			} else {
				if (nameFreq.get(mostCommonName) < nameFreq.get(name)) { mostCommonName = name;}
			}
    	}
    	
    	public void combine(NameDataStruct struct) {
    		ConcurrentMap<String, Integer> temp = Stream.of(this.nameFreq, struct.nameFreq)
    				.parallel()
    				.map(Map::entrySet)
    				.flatMap(Collection::stream)
    			    .collect(
    			    		Collectors.toConcurrentMap(
    			    				   Map.Entry::getKey, Map.Entry::getValue, (a, b) -> (a + b)));
    		nameFreq = temp;
    		if (struct.nameFreq.get(struct.mostCommonName) > nameFreq.get(mostCommonName)) {
    			mostCommonName = struct.mostCommonName; 
    		}    		
    	}
    }
    */

    /**
     * Sequentially computes the number of students who have failed the course
     * who are also older than 20 years old. A failing grade is anything below a
     * 65. A student has only failed the course if they have a failing grade and
     * they are not currently active.
     *
     * @param studentArray Student data for the class.
     * @return Number of failed grades from students older than 20 years old.
     */
    public int countNumberOfFailedStudentsOlderThan20Imperative(
            final Student[] studentArray) {
        int count = 0;
        for (Student s : studentArray) {
            if (!s.checkIsCurrent() && s.getAge() > 20 && s.getGrade() < 65) {
                count++;
            }
        }
        return count;
    }

    /**
     * TODO compute the number of students who have failed the course who are
     * also older than 20 years old. A failing grade is anything below a 65. A
     * student has only failed the course if they have a failing grade and they
     * are not currently active. This should mirror the functionality of
     * countNumberOfFailedStudentsOlderThan20Imperative. This method should not
     * use any loops.
     *
     * @param studentArray Student data for the class.
     * @return Number of failed grades from students older than 20 years old.
     */
    public int countNumberOfFailedStudentsOlderThan20ParallelStream(
            final Student[] studentArray) {
        return (int) Stream.of(studentArray)
        		.parallel()
        		.filter(a -> (!a.checkIsCurrent() && a.getAge() > 20 && a.getGrade()< 65))
        		.count();
        		
    }
}
