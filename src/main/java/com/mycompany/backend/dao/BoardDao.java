package com.mycompany.backend.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.mycompany.backend.dto.Board;
import com.mycompany.backend.dto.Pager;

@Mapper
public interface BoardDao {
	public List<Board> selectByPage(Pager pager);
	public int count();
	public List<Board> selectAll();
	public List<Board> searchBoard(String word); 
	public Board selectByBno(int bno);
	public int insert(Board board);
	public int deleteByBno(int bno);
	public int update(Board board);
	public int updateBhitcount(int bno);
  
}

