package com.portfoliogenerator.service;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class IdGeneratorService {
	private static final String CHARACTERS = "abcdefghijklmnopqrstuvwxyz";
	private static final int ID_LENGTH = 5;
	private static final SecureRandom random = new SecureRandom();

	public String generateUniqueId(){
		return IntStream.range(0, ID_LENGTH)
				.mapToObj(i -> String.valueOf(CHARACTERS.charAt(random.nextInt(CHARACTERS.length()))))
				.collect(Collectors.joining());
	}
}
